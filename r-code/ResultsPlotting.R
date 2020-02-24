source("GeneralPlotting.R")
library(fields) # image.plot and colors
library(plyr) # list to df

library(sp) # tracks
library(adehabitatHR) #tracks


# colors
cols = tim.colors( 256 )

processFile = function(dataFile, landscapeFile)
{
	# also allow passing data in multiple files
	data = read.csv(dataFile[1], as.is = c("TotalConsumption0", "StdDevConsumption0", "DistanceTraveled0", 
										   "Sinuosity0", "AverageSpeed0", "AverageSpeedSearch0", "AverageSpeedFeeding0", 
										   "MeanSinTurningAngle0", "MeanCosTurningAngle0"), header = TRUE)
	# TODO why are these being read in as factors/character data? grep for non-numeric values
	idx1 = which(names(data) == "TotalConsumption0")
	idx2 = which(names(data) == "MeanCosTurningAngle0")
	data[,idx1:idx2] = apply(data[,idx1:idx2], 2, as.numeric)
	
	if (length(dataFile) > 1)
	{
		for (i in 2:length(dataFile))
		{
			data = rbind(data, read.csv(dataFile[i], header = TRUE))
		}
	}
	
	# get rid of long decay = short decay (unless both 1)
	data = subset(data, !(LongDecayRate == ShortDecayRate) | LongDecayRate == 1 )
	
	# is ordering necessary?
	if (!is.null(landscapeFile))
	{
		data = processLandscapes(data, landscapeFile)
		data = data[order(data$mean, data$scale, data$LongDecayRate, data$ShortDecayRate),]
	}
	else
	{
		data = data[order(data$LongDecayRate, data$ShortDecayRate),]
	}
	
	#unique(paste(data$LongDecayRate, data$ShortDecayRate))
	#unique(paste(data$mean, data$scale))
	
	# convert from factor to boolean
#	data$MemoryCorrelation = as.logical(data$MemoryCorrelation)
	data$MemoryIsFullyInformed = as.logical(data$MemoryIsFullyInformed)
	
	data$memParam = factor(paste(data$LongDecayRate, data$ShortDecayRate, sep = ", "), ordered = TRUE,
							  levels = unique(paste(data$LongDecayRate, data$ShortDecayRate, sep = ", ")) )
	data$memParamFull = factor(paste(data$LongDecayRate, data$ShortDecayRate, data$ShortMemoryFactor, data$MemorySpatialScaleForaging, sep = ", "), 
						  ordered = TRUE,
						  levels = unique(paste(data$LongDecayRate, data$ShortDecayRate, data$ShortMemoryFactor, data$MemorySpatialScaleForaging, sep = ", ")) )
	
	data$LongDecayRate[data$MovementType == "Kinesis"] = "K"
	data$ShortDecayRate[data$MovementType == "Kinesis"] = "K"
	data$ShortMemoryFactor[data$MovementType == "Kinesis"] = "K"
	data$MemorySpatialScaleForaging[data$MovementType == "Kinesis"] = "K"
	data$memParam[data$MovementType == "Kinesis"] = NA
	data$memParamFull[data$MovementType == "Kinesis"] = NA

	# R for random walk
	data$LongDecayRate[data$MovementType == "SingleState"] = "R" 
	data$ShortDecayRate[data$MovementType == "SingleState"] = "R"
	data$ShortMemoryFactor[data$MovementType == "SingleState"] = "R"
	data$MemorySpatialScaleForaging[data$MovementType == "SingleState"] = "R"
	data$memParam[data$MovementType == "SingleState"] = NA
	data$memParamFull[data$MovementType == "SingleState"] = NA


	return(data)
}

processLandscapes = function(data, landscapeFile)
{
	data$id = as.numeric(substr(data$ResourceLandscapeFileName, 5, 
								regexpr("\\.", data$ResourceLandscapeFileName) - 1))
	landProps = read.csv(landscapeFile, header = TRUE)
	borderProps = expand.grid(id = landProps$id, EmptyBorderSize = unique(data$EmptyBorderSize), landPercentZero = NA)
	for (i in 1:nrow(borderProps))
	{
		land = read.csv(file = paste0("model/land/land", borderProps$id[i], ".csv"), header = TRUE)
		land = addBorder(land, borderProps$EmptyBorderSize[i])
		borderProps$landPercentZero[i] = sum(land == 0) / nrow(land) / ncol(land)
	}
	
	landProps = merge(landProps, borderProps, "id")
	data = merge(data, landProps, c("id", "EmptyBorderSize"))
	
	data$landParam = factor(paste(data$mean, data$scale, sep = ", "), ordered = TRUE, 
							levels = unique(paste(data$mean, data$scale, sep = ", ")) )
	
	return(data)
}

processTracks = function(data, trackDir)
{
	require(sp)
	require(adehabitatHR)
	
	data$MCP95 = NA
	data$MCP95Last = NA
	data$MSD = NA # mean squared displacement
	
	for (i in 1:nrow(data))
	{
		track = read.csv(paste0(trackDir, "/Tracks", data$SimulationIndex[i], ".csv"), colClasses = c("integer", "numeric", "numeric"), header = TRUE)
		last = floor(nrow(track) * 0.8):nrow(track)
		
		data$MSD[i] = mean( (track$x - track$x[1])^2 + (track$y - track$y[1])^2 ) 
		track = SpatialPoints(track[,2:3]) # x and y are now in @coords
		data$MCP95[i] = mcp(track, percent = 95)$area
		data$MCP95Last[i] = mcp(track[last], percent = 95)$area	
		
	}
	return(data)
}

addBorder = function(land, borderSize)
{
	if (borderSize > 0)
	{
		for (i in 1:borderSize)
		{
			land = rbind(0, land, 0)
			land = cbind(0, land, 0)
		}
	}
	return(land)
}

plotLongVsShort = function(data, zlim = NULL, legend = TRUE)
{
	dataMat = xtabs(TotalConsumption0 ~ LongDecayRate + ShortDecayRate, data=data)
	dataMat[dataMat == 0] = NA
	if (is.null(zlim))
	{
		zlim = range(dataMat, na.rm = TRUE)
	}
	image(dataMat, col = cols, zlim = zlim,
		  xaxt = "n", yaxt="n", xlab = "Long decay rate", ylab = "Short decay rate")
	axis(1, at = seq( 0, 1, len = nrow(dataMat)), labels = rownames(dataMat))
	axis(2, at = seq( 0, 1, len = ncol(dataMat)), labels = colnames(dataMat), las = 1)
	
	if(legend)
	{
		image.plot(legend.only = TRUE, zlim = zlim, col = cols, horizontal = FALSE,
				   axis.args=list( at = min(dataMat, na.rm = TRUE) + diff(range(dataMat, na.rm = TRUE)) * c(0.2, 0.8), 
				   				labels = c("low", "high"), tick = FALSE ))
	}
}

# data in matrix suita
plotMemoryVsLandscape = function(data, colIdx)
{
	dataMat = xtabs(data[,colIdx] ~ landParam + memParam, data = data)
	nrows = dim(dataMat)[1]
	ncols = dim(dataMat)[2]
	nmeans = length(unique(data$mean))
	xs = seq(0, 1, len = nrows + 1)
	ys = seq(0, 1, len = ncols + 1)
	scales = paste(unique(data$scale), collapse = ", ")
	
	image(x = xs, y = ys, dataMat, col = cols,
		  xaxt = "n", yaxt="n", xlab = paste("mean\n(scale = ", scales, ")", sep = ""), ylab = "")
	axis(1, at=seq(0.5 / nmeans, 1 - 0.5 / nmeans, len = nmeans), labels=unique(data$mean))
	axis(2, at=seq(0.5 / ncols, 1 - 0.5 / ncols, len = ncols), labels=colnames(dataMat), las = 1)
	mtext("long decay, short decay", side = 2, las = 0, line = 6)

	memParamCombos = unique(cbind(data$LongDecayRate, data$ShortDecayRate))
	longBounds = cumsum(table(memParamCombos[,1])) + 1 # when boh have same values: cumsum(8:1)
	abline(h = ys[longBounds]) 
	abline(v = seq(0, 1, len = nmeans + 1))
	
	image.plot(legend.only = TRUE, zlim = range(dataMat), col = cols, horizontal = FALSE, legend.width = 2,
			   axis.args=list( at = min(dataMat) + diff(range(dataMat)) * c(0.2, 0.8), labels = c("low", "high"), tick = FALSE, cex.axis = 1 ))
	
}

plotMovementVsPredator = function(dataMat, yAxisLabs, ylab = "Predators, hard <--> easy", zlim = NULL)
{
	nrows = dim(dataMat)[1]
	ncols = dim(dataMat)[2]
	nKinesis = length(grep("K", rownames(dataMat)))
	xs = seq(0, 1, len = nrows + 1)
	ys = seq(0, 1, len = ncols + 1)
	memStart = xs[nKinesis + 1]
	if (is.null(zlim)) zlim = range(dataMat)
	
	#param combos, assume 1st is memory vs kinesis, then plot next 2 on axis, dropping kinesis values
	param2 = sapply(strsplit(rownames(dataMat), " "), function(x) x[2])[-(1:nKinesis)]
	nparam2 = length(unique(param2))
	nparam2Int = (1 - memStart) / nparam2 # length of one interval
	param3 = sapply(strsplit(rownames(dataMat), " "), function(x) x[3])[-(1:nKinesis)]
	nparam3 = length(unique(param3))
	nparam3Int = nparam2Int / nparam3 # length of one interval within param2 interval
	
	image(x = xs, y = ys, dataMat, col = cols, zlim = zlim,
		  xaxt = "n", yaxt="n", xlab = "Movement", ylab = ylab)
	axis(1, at=memStart / 2, labels="K")
	axis(1, at=seq(memStart + nparam2Int / 2, 1 - nparam2Int / 2, len = nparam2), labels=unique(param2))
	axis(1, at=seq(memStart + nparam3Int / nparam3, memStart + nparam2Int - nparam3Int / nparam3, len = nparam3), labels=unique(param3), line = 1.5)
	axis(2, at=seq(0.5 / length(yAxisLabs), 1 - 0.5 / length(yAxisLabs), len = length(yAxisLabs)), labels=yAxisLabs, las = 1)
	
	abline(v = memStart)
	abline(v = seq(memStart + nparam2Int, 1 - nparam2Int, nparam2Int), col = "gray20")
	#abline(v = seq(memStart + nparam3Int, 1 - nparam3Int, nparam3Int), col = "gray50")
	
	abline(h = 1:(length(yAxisLabs) - 1) / length(yAxisLabs), col = "gray20")
}

plotParameterImage = function(dataMat, xlab = "", ylab = "", zlim = NULL)
{
	nrows = dim(dataMat)[1]
	ncols = dim(dataMat)[2]
		xs = seq(0, 1, len = nrows + 1)
	ys = seq(0, 1, len = ncols + 1)
	if (is.null(zlim)) zlim = range(dataMat)
	
	image(x = xs, y = ys, dataMat, col = cols, zlim = zlim, xaxt = "n", yaxt = "n", xlab = xlab, ylab = ylab)
	axis(1, at=seq(0.5 / ncols, 1 - 0.5 / ncols, len = ncols), labels = row.names(dataMat))
	axis(2, at=seq(0.5 / ncols, 1 - 0.5 / ncols, len = ncols), labels = colnames(dataMat), las = 1)
}

plotMultipleMeans = function(data)
{
	meanZlim = range(tapply(data$TotalConsumption0, list(data$LongDecayRate, data$ShortDecayRate, data$mean), sum), na.rm = TRUE)
	for (m in unique(data$mean))
	{
		dataSub = subset(data, mean == m)
		plotLongVsShort(dataSub, zlim = meanZlim, legend = FALSE)
		title(paste("mean =", m))
	}
}

plotMultipleScales = function(data)
{
	scaleZlim = range(tapply(data$TotalConsumption0, list(data$LongDecayRate, data$ShortDecayRate, data$scale), sum), na.rm = TRUE)
	for (s in unique(data$scale))
	{
		dataSub = subset(data, scale == s)
		plotLongVsShort(dataSub, zlim = scaleZlim, legend = FALSE)
		title(paste("scale =", s))
	}
}

plotCompareKinesisMemoryPreds = function(data)
{
	dataK = subset(data, MovementType == "Kinesis")
	dataM = subset(data, MovementType == "MemoryDirectional")
	dataM$ForagerAnglePersistanceSearch[dataM$MemoryCorrelation == TRUE] = 0
	numMPersistence = length(unique(dataM$ForagerAnglePersistanceSearch))
	numPDuration = length(unique(dataM$PredatorDuration))
	numPTradeoff = length(unique(dataM$PredatorRandomness))
	
	width = 0.25

	# frame = FALSE doesn't seem to work?
	par(mfrow = c(2, 3), mar = c(1, 1, 1, 1) + 0.1, mgp = c(2.5, 0.5, 0), oma = c(3, 3, 0, 0))
	boxplot(dataM$TotalPredatorEncounters0 ~ dataM$ForagerAnglePersistanceSearch, ylim = c(0, 200), boxwex = width,
			at = 1:numMPersistence - 0.2, col = "darkgreen", xaxt = "n")
	boxplot(dataK$TotalPredatorEncounters0 ~ dataK$ForagerAnglePersistanceSearch, ylim = c(0, 200), boxwex = width,
			at = 1:length(unique(dataK$ForagerAnglePersistanceSearch)) + 0.2, col = "purple", xaxt = "n", add = TRUE)
	mtext(text = "Predator encounters", side = 2, line = 2.5, xpd = TRUE)
	axis(side = 1, at = 1:3, labels = c("Variable", 0.5, 0.8))
		
	boxplot(dataM$TotalPredatorEncounters0 ~ dataM$PredatorDuration, ylim = c(0, 200), boxwex = width,
			at = 1:numPDuration - 0.2, col = "darkgreen", xaxt = "n")
	boxplot(dataK$TotalPredatorEncounters0 ~ dataK$PredatorDuration, ylim = c(0, 200), boxwex = width,
			at = 1:numPDuration + 0.2, col = "purple", xaxt = "n", add = TRUE)
	axis(side = 1, at = 1:numPDuration, labels = sort(unique(dataM$PredatorDuration)))
	
	boxplot(dataM$TotalPredatorEncounters0 ~ dataM$PredatorRandomness, ylim = c(0, 200), boxwex = width,
			at = 1:numPTradeoff - 0.2, col = "darkgreen", xaxt = "n", xlab = "", ylab = "")
	boxplot(dataK$TotalPredatorEncounters0 ~ dataK$PredatorRandomness, ylim = c(0, 200), boxwex = width,
			at = 1:numPTradeoff + 0.2, col = "purple", xaxt = "n", add = TRUE)
	axis(side = 1, at = 1:numPTradeoff, labels = sort(unique(dataM$PredatorRandomness)))

	#####
	boxplot(dataM$TotalConsumption0 ~ dataM$ForagerAnglePersistanceSearch, boxwex = width,
			at = 1:numMPersistence - 0.2, col = "darkgreen", xaxt = "n", xlab = "Search angle persistence", ylab = "Total consumption",
			xpd = TRUE)
	boxplot(dataK$TotalConsumption0 ~ dataK$ForagerAnglePersistanceSearch, boxwex = width,
			at = 1:length(unique(dataK$ForagerAnglePersistanceSearch)) + 0.2, col = "purple", xaxt = "n", add = TRUE)
	mtext(text = "Total consumption", side = 2, line = 2.5, xpd = TRUE)
	mtext(text = "Search angle persistence", side = 1, line = 2.5, xpd = TRUE)
	axis(side = 1, at = 1:3, labels = c("Variable", 0.5, 0.8))
	
	boxplot(dataM$TotalConsumption0 ~ dataM$PredatorDuration, boxwex = width,
			at = 1:numPDuration - 0.2, col = "darkgreen", xaxt = "n", xlab = "Predator duration", ylab = "", xpd = TRUE)
	boxplot(dataK$TotalConsumption0 ~ dataK$PredatorDuration, boxwex = width,
			at = 1:numPDuration + 0.2, col = "purple", xaxt = "n", add = TRUE)
	mtext(text = "Predator duration", side = 1, line = 2.5, xpd = TRUE)
	axis(side = 1, at = 1:numPDuration, labels = sort(unique(dataM$PredatorDuration)))
	
	boxplot(dataM$TotalConsumption0 ~ dataM$PredatorRandomness, boxwex = width,
			at = 1:numPTradeoff - 0.2, col = "darkgreen", xaxt = "n", xlab = "Tradeoff proportion", ylab = "", xpd = TRUE)
	boxplot(dataK$TotalConsumption0 ~ dataK$PredatorRandomness, boxwex = width,
			at = 1:numPTradeoff + 0.2, col = "purple", xaxt = "n", add = TRUE)
	mtext(text = "Tradeoff proportion", side = 1, line = 2.5, xpd = TRUE)
	axis(side = 1, at = 1:numPTradeoff, labels = sort(unique(dataM$PredatorRandomness)))
	
}

plotCompareMemoryParamsPreds = function(data)
{
	par(mfrow = c(2, 3), mar = c(1, 1, 1, 1) + 0.1, mgp = c(2.5, 0.5, 0), oma = c(3, 3, 0, 0))
	
	boxplot(data$TotalPredatorEncounters0 ~ data$MemorySpatialScaleForaging, ylim = c(0, 200), 
			col = "darkgreen", xlab = "", ylab = "")
	mtext(text = "Predator encounters", side = 2, line = 2.5, xpd = TRUE)

	boxplot(data$TotalPredatorEncounters0 ~ data$PredatorDecayRate, ylim = c(0, 200), 
			col = "darkgreen", xlab = "", ylab = "")
	
	boxplot(data$TotalPredatorEncounters0 ~ data$ForagerAnglePersistanceSearch, ylim = c(0, 200), 
			col = "darkgreen", xlab = "", ylab = "")
	
	###
	boxplot(data$TotalConsumption0 ~ data$MemorySpatialScaleForaging, 
			col = "darkgreen", xlab = "", ylab = "")
	mtext(text = "Total consumption", side = 2, line = 2.5, xpd = TRUE)
	mtext(text = "Memory spatial scale", side = 1, line = 2.5, xpd = TRUE)
	
	boxplot(data$TotalConsumption0 ~ data$PredatorDecayRate, 
			col = "darkgreen", xlab = "", ylab = "")
	mtext(text = "Predator decay rate", side = 1, line = 2.5, xpd = TRUE)
	
	boxplot(data$TotalConsumption0 ~ data$ForagerAnglePersistanceSearch, 
			col = "darkgreen", xlab = "", ylab = "")
	mtext(text = "Memory correlation", side = 1, line = 2.5, xpd = TRUE)
}

plotConsumpAndEnountersByPredParams = function(data)
{
	par(mfrow = c(3, 6), mar = c(3.5, 3.5, 1, 1) + 0.1, mgp = c(2.5, 0.5, 0))
	boxplot(data$PredatorReEncounters0 ~ data$mean, ylim = c(0, 200), ylab = "Pred RE encounters")
	boxplot(data$PredatorReEncounters0 ~ data$MemorySpatialScaleForaging, ylim = c(0, 200))
	boxplot(data$PredatorReEncounters0 ~ data$PredatorDuration, ylim = c(0, 200))
	boxplot(data$PredatorReEncounters0 ~ data$PredatorRandomness, ylim = c(0, 200))
	boxplot(data$PredatorReEncounters0 ~ data$ForagerAnglePersistanceSearch, ylim = c(0, 200))
	boxplot(data$PredatorReEncounters0 ~ data$ForagerAnglePersistanceFeeding, ylim = c(0, 200))
	boxplot(data$TotalPredatorEncounters0 ~ data$mean, ylim = c(0, 200), ylab = "Pred encounters")
	boxplot(data$TotalPredatorEncounters0 ~ data$MemorySpatialScaleForaging, ylim = c(0, 200))
	boxplot(data$TotalPredatorEncounters0 ~ data$PredatorDuration, ylim = c(0, 200))
	boxplot(data$TotalPredatorEncounters0 ~ data$PredatorRandomness, ylim = c(0, 200))
	boxplot(data$TotalPredatorEncounters0 ~ data$ForagerAnglePersistanceSearch, ylim = c(0, 200))
	boxplot(data$TotalPredatorEncounters0 ~ data$ForagerAnglePersistanceFeeding, ylim = c(0, 200))
	boxplot(data$TotalConsumption0 ~ data$mean, main = "mean", ylab = "Consumption" )
	boxplot(data$TotalConsumption0 ~ data$MemorySpatialScaleForaging, main = "memory sp scale")
	boxplot(data$TotalConsumption0 ~ data$PredatorDuration, main = "pred dur")
	boxplot(data$TotalConsumption0 ~ data$PredatorRandomness, main = "tradeoff")
	boxplot(data$TotalConsumption0 ~ data$ForagerAnglePersistanceSearch, main = "persis search")
	boxplot(data$TotalConsumption0 ~ data$ForagerAnglePersistanceFeeding, main = "persis feed")
}

plotPredConsumpScatter = function(data, plotFactor)
{
	dataPlot = data
	pchs = 0:18
	color = getColors()
	dataPlot$PredatorReEncounters0[dataPlot$PredatorReEncounters0 > 100] = 100
	plot(x = dataPlot$TotalConsumption0, y = dataPlot$PredatorReEncounters0, col = color[plotFactor],
		 xlab = "Total consumption", ylab = "Predator encounters")
	legend("topright", levels(plotFactor), col= color[1:length(levels(plotFactor))], pch = 19, legend = levels(plotFactor))
}

plotFoodSafetyTradeoff = function(data, responseIdx, covariateIdx, ylab, legendPos = NULL, legendTitle = "")
{
	plotTwoParamByLine(data, responseIdx, which(names(data) == "FoodSafetyTradeoff"),
					   covariateIdx, "Food safety tradeoff", ylab, legendPos, legendTitle)
}

plotTwoParamByLine = function(data, responseIdx, predIdx, covariateIdx, xlab, ylab, lty = 1, add = FALSE, legendPos = NULL, legendTitle = NULL, ...)
{
	mat = tapply(data[,responseIdx], list(data[,predIdx], data[,covariateIdx]), mean) # bty = "l" doesn't work
	cols = getColors(alpha = 1)[1:ncol(mat)]
	matplot(mat, type = "l", xaxt = "n", xlab = xlab,  ylab = ylab, col = cols, lty = lty, add = add, ...)
	axis(1, at = 1:nrow(mat), labels = rownames(mat))
	if (!is.null(legendPos))
	{
		legend(legendPos, col = cols, lty = 1, lwd = 1.5, legend = colnames(mat), bty = "n", title = legendTitle)
	}
}

# title and left title can be strings
plotHabitatUse = function(dataPred, dataForaging, title = FALSE, lefttitle = FALSE)
{
	colors = colorRampPalette(c("white", "black"))(6)
	
	# dataPred already subset for mean and scale
	numZero = mean(dataPred$landPercentZero) * dataPred$NumSteps[1] 
	numNonZero = ( 1 - mean(dataPred$landPercentZero)) * dataPred$NumSteps[1] 
	
	bestParams = NULL
	
	# for each predator combo
	loopIdx = 1
	for (duration in c(10, 1000))
	{
		for (randomness in c(0, 1))
		{
			datasub = subset(dataPred, PredatorDuration == duration & PredatorRandomness == randomness)
			
			# find best high and low survival (assume not R or K)
			highS = tapply(datasub$highSur, datasub$ParamCombo, mean)
			lowS = tapply(datasub$lowSur, datasub$ParamCombo, mean)
			highSParam = names(highS)[which.max(highS)]
			lowSParam = names(lowS)[which.max(lowS)]
			datasub$ParamCombo[datasub$ParamCombo == highSParam] = "aH" # alphabetize before K
			datasub$ParamCombo[datasub$ParamCombo == lowSParam] = "aL"
			datasub = subset(datasub, ParamCombo %in% c("aH", "aL", "K", "R"))
			
			datamat = laply(list(datasub$X0Bin0, datasub$X0Bin1, datasub$X0Bin2, datasub$X0Bin3, datasub$X0Bin4), 
							function(x) tapply(x, datasub$ParamCombo, mean))
			datamat = cbind(datamat, L = c(numZero, rep(0.25 * numNonZero, 4)))
			print(datamat)
			barplot(height = datamat, width = c(rep(1, 4), 0.25), col = colors,
					xlab = "", yaxt = if(loopIdx == 1) "s" else "n", 
					names.arg = c("M+", "M-", "K", "R", "L"), cex.names = 0.9)
			if (title == TRUE)
			{
				mtext( bquote(delta == .(duration) ~~ rho == .(randomness)), side = 3, line = 1)
			}
			else if (length(title) > 1)
			{
				mtext( title[loopIdx], side = 3, line = 1, xpd = NA)
			}
			if (lefttitle != FALSE & loopIdx == 1)
			{
				mtext(lefttitle, side = 2, line = 2.5, las = 0, xpd = NA)
			}
			
			loopIdx = loopIdx + 1
			bestParams = rbind(bestParams, c(duration, randomness, highSParam, lowSParam))
		}
	}
	
	# compare to no predators, dataForaging should already be subset for mean, scale and foraging memory parameters
	datamat = laply(list(dataForaging$X0Bin0, dataForaging$X0Bin1, dataForaging$X0Bin2, dataForaging$X0Bin3, dataForaging$X0Bin4), 
					function(x) tapply(x, dataForaging$MovementType, mean))
	numZero = mean(dataForaging$landPercentZero) * dataForaging$NumSteps[1] # different than pred b/c no empty border
	numNonZero = ( 1 - mean(dataForaging$landPercentZero)) * dataForaging$NumSteps[1] 
	datamat = cbind(datamat, L = c(numZero, rep(0.25 * numNonZero, 4)))
	print(datamat)
	barplot(height = datamat, width = c(rep(1, length(unique(datasub$MovementType))), 0.5), col = colors,
			xlab = "", yaxt = "n", names.arg = c("M", "K", "R", "L"))
	if (title == TRUE)
	{
		mtext( "No predation", side = 3, line = 1)
	}
	else if (length(title) > 1)
	{
		mtext( title[loopIdx], side = 3, line = 1)
	}
	
	
	return(bestParams)
}

plotHabitatUseKinesis = function(data)
{
	colors = colorRampPalette(c("white", "black"))(6)
	
	# already verified that predator duration/randomness doesn't affect time allocation for kinesis
	for (m in unique(data$mean))
	{
		for (s in unique(data$scale))
		{
			datasub = subset(data, mean == m & scale == s
							 & ForagerAnglePersistanceSearch %in% c(0.1, 0.3, 0.5, 0.7, 0.9) & ForagerAnglePersistanceFeeding == 0.8)
			
			datamat = laply(list(datasub$X0Bin0, datasub$X0Bin1, datasub$X0Bin2, datasub$X0Bin3, datasub$X0Bin4), 
							function(x) tapply(x, datasub$ForagerAnglePersistanceSearch, mean))
			numZero = mean(datasub$landPercentZero) * datasub$NumSteps[1] # num steps should be same for all
			numNonZero = ( 1 - mean(datasub$landPercentZero)) * datasub$NumSteps[1] 
			datamat = cbind(datamat, L = c(numZero, rep(0.25 * numNonZero, 4)))
			barplot(height = datamat, width = c(rep(1, length(unique(datasub$ForagerAnglePersistanceSearch))), 0.25), col = colors,
					main = paste("mean =", m, "scale =", s), xlab = "Search persistence")
		}
	}
}

plotHabitatUseForaging = function(data, labelLand = TRUE)
{
	colors = colorRampPalette(c("white", "black"))(6)
	
	i = 0
	numPlots = length(unique(data$scale)) * length(unique(data$mean))
	for (s in unique(data$scale))
	{
		for (m in unique(data$mean))
		{
			i = i + 1
			labels = if(i > numPlots / 2) c("M", "K", "R", "L") else rep("", 4)
			datasub = subset(data, mean == m & scale == s)
			
			datamat = laply(list(datasub$X0Bin0, datasub$X0Bin1, datasub$X0Bin2, datasub$X0Bin3, datasub$X0Bin4), 
							function(x) tapply(x, datasub$MovementType, mean))
			# really dividing by interval size is a workaround because reporter should be taking that into account
			numZero = mean(datasub$landPercentZero) * datasub$NumSteps[1] / datasub$IntervalSize[1] # num steps should be same for all
			numNonZero = ( 1 - mean(datasub$landPercentZero)) * datasub$NumSteps[1] / datasub$IntervalSize[1]
			datamat = cbind(datamat, L = c(numZero, rep(0.25 * numNonZero, 4)))
			barplot(height = datamat, width = c(rep(1, length(unique(datasub$MovementType))), 0.5), col = colors,
					xlab = "", names.arg = labels, yaxt = 'n')
			
			if (labelLand & i <= length(unique(data$mean))) 
			{ 
				mtext(bquote(mu[Q] == .(m)), side = 3, line = 1, font = 2, cex = 0.7) 
			}
			if (i %in% c(1, numPlots / 2 + 1)) 
			{ 
				axis(side = 2, las = 1, xpd = TRUE)
				
				if (labelLand)
				{
					mtext(bquote(gamma[Q] == .(s)), side = 2, line = 3.5, font = 2, cex = 0.7)
				}
			}
			if (i == 1)
			{
				mtext("Time", side = 2, at = 0, line = 2.8, cex = 0.8, xpd = TRUE)
			}
			if (i == ceiling(mean(c(numPlots, numPlots/2)))) # center of bottom
			{
				mtext("Movement model", side = 1, line = 2, cex = 0.8, xpd = TRUE)
			}
		}
	}
}


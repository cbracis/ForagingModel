createScatterPlot = function(fast, slow, columnIdx, xlab, transparency = 1, extras = TRUE)
{
	cols = c(getColor("darkblue", transparency), getColor("firebrick3", transparency)) # fast, slow
	fast$col = 1
	slow$col = 2
	all = rbind(fast, slow)
	all = all[order(all$scaledConsump),] # so points will be interleaved when plotted
	ylab = ifelse(extras, yes = "Relative consumption", no = "")
	
	plot(all[,columnIdx], all$scaledConsump, col = cols[all$col],
		 xlab = xlab, ylab = ylab)
	abline(h = 0, col = "gray70")
	if (extras)
	{
		legend("topright", legend = c("fast species", "slow species"), fill = c("darkblue", "firebrick3"), bty = "n")
	}
}

createScatterPlotsLandStats = function(first, second)
{
	createScatterPlot(first, second, 
					  which(names(first) == "sd"), "Resource standard deviation", transparency = 0.5)
	createScatterPlot(first, second,
					  which(names(first) == "moransI"), "Resource Moran's I", transparency = 0.5, extras = FALSE)
	createScatterPlot(first, second,
					  which(names(first) == "fractal"), "Resource fractal dimension", transparency = 0.5, extras = FALSE)
}

createBoxPlot = function(data)
{
	data = data[order(data$scale, data$nu, data$mean),]
	groupBy = factor(paste(data$scale, data$nu, data$mean), levels = unique(paste(data$scale, data$nu, data$mean)))
	#print(levels(groupBy))
	cols = c("gray90", "gray85", "gray80", "gray75", "gray70") # in order, so recycling this vector works
	boxplot(data$scaledConsump ~ groupBy, col = cols, outline = FALSE, border = FALSE, boxlty = 0, whisklty = 0, medcol = "red", range = 1, 
			ylim = quantile(data$scaledConsump, c(0.02, 0.98)), xaxt = "n", xlab = "nu", ylab = "Relative consumption")
	axis(side = 1, at = seq(from = 2.5, to = 173.5, by = 5), labels = rep(c(0.5, 1, 2, 5, 100), 7), cex.axis = 0.5)
	abline(h = 0, col = "gray60")
	abline(v = seq(from = 25.5, to = 155.5, by = 25), col = "gray60")
	text(x = seq(from = 12.5, to = 167.5, by = 25), y = rep(quantile(data$scaledConsump, 0.02), 7), labels = paste("scale =", unique(data$scale)))
	
}

creatBoxPlotSubset = function(data)
{
	data = data[order(data$scale, data$mean),]
	groupBy = factor(paste(data$scale, data$mean), levels = unique(paste(data$scale, data$mean)))
	print(levels(groupBy))
	boxplot(data$scaledConsump ~ groupBy, col = "gray80", outline = FALSE, border = FALSE, 
			boxlty = 0, whisklty = 0, medcol = "red", range = 1, 
			ylim = quantile(data$scaledConsump, c(0.02, 0.98)), xaxt = "n", xlab = "mean", ylab = "Relative consumption")
	axis(side = 1, at = 1:120, labels = rep(seq(-2, 1.5, 0.5), 15), cex.axis = 0.5)
	abline(h = 0, col = "gray60")
	abline(v = seq(from = 8.5, to = 112.5, by = 8), col = "gray85")
	text(x = seq(from = 4, to = 116, by = 8), y = rep(quantile(data$scaledConsump, 0.02), 8), labels = paste("scale\n", unique(data$scale)))
}

getColor = function(color, transparency)
{
	temp = col2rgb(color) / 255
	color = rgb(temp[1], temp[2], temp[3], alpha = transparency)
	return(color)
}


#library(manipulate)
library(RandomFields) # generate Gaussian random fields
#library(fields) # image.plot

# from sequential greens RColorBrewer pallette
habitatCols = c("white", colorRampPalette(c("#c7e9c0", "#a1d99b", "#74c476", "#41ab5d", "#238b45", "#006d2c", "#00441b"))(1000))
habitatColsBW = c("white", colorRampPalette(c("gray90", "black"))(1000))

generateMatern = function(mean, scale, nu, normalize = TRUE)
{
	x = seq(1, 50, 1)
	z = GaussRF(x = x, y = x, grid = TRUE, model = "matern", param = c(mean=mean, variance=1, nugget=0, scale=scale, nu=nu))
	if (normalize)
	{
		z = normalizeZ(z)
	}
	print(range(z))
	return(z)
}

plotMatern = function(mean, scale, nu, normalize = TRUE)
{
	fields = list(NULL)
	for (i in 1:length(mean))
	{
		fields[[i]] = generateMatern(mean[i], scale[i], nu[i], normalize)
	}
	ranges = sapply(fields, range)
	
	for (i in 1:length(mean))
	{
		image(fields[[i]], zlim = range(ranges), col = habitatCols, axes = FALSE)
		title( paste("m =", mean[i], "s =", scale[i], "n =", nu[i]))
		#text(x = 1, y = 49, labels = range(z), pos = 4)
	}
}

normalizeZ = function(z, total = 1)
{
	# make positive
	z[z < 0] = 0
	
	if (sum(z) == 0) print("Invalid field, all 0")
	
	# sum to one (or other amount for larger landscapes)
	z = z * total / sum(z) 
	
	return(z)
}

plotLandscape = function(z, zlim = NULL, col = habitatCols, title = "", axes = FALSE, ...)
{
	if (is.null(zlim)) { zlim = range(z) }
	xVals = seq(from = 0, to = nrow(z), length = nrow(z) + 1)
	yVals = seq(from = 0, to = ncol(z), length = ncol(z) + 1)
	
	image(1:nrow(z), 1:ncol(z), z, zlim = zlim, col = col, asp = 1,
		  xlab = "", ylab = "", axes = axes, ...)
	if (axes == FALSE) { box() }
		title( title )
}

plotLandscapeNoWhite = function(z, zlim = NULL, title = "", axes = FALSE)
{
	if (is.null(zlim)) { zlim = range(z) }
	xVals = seq(from = 0, to = nrow(z), length = nrow(z) + 1)
	yVals = seq(from = 0, to = ncol(z), length = ncol(z) + 1)
		
	image(xVals, yVals, z, zlim = zlim, col = habitatCols[-1], asp = 1,
		  xlab = "", ylab = "", axes = axes)
	if (axes == FALSE) { box() }
		title( title )
}

plotLandscapeBW = function(z, zlim = NULL, title = "", axes = FALSE)
{
	if (is.null(zlim)) { zlim = range(z) }
	xVals = seq(from = 0, to = nrow(z), length = nrow(z) + 1)
	yVals = seq(from = 0, to = ncol(z), length = ncol(z) + 1)
	
	image(xVals, yVals, z, , zlim = zlim, col = habitatColsBW, asp = 1,
		  xlab = "", ylab = "", axes = axes)
	if (axes == FALSE) { box() }
	title( title )
}




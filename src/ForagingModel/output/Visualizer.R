habitatCols = c(colorRampPalette(c("gainsboro", "darkolivegreen3"))(10), colorRampPalette(c("darkolivegreen3", "darkolivegreen"))(64))
memoryCols = colorRampPalette(c("firebrick4", "white", "dodgerblue"))(64)
foragerPch = c(15, 16, 17, 18) # by state
predatorPch = 18
destinationPch = 4
endPch = 19
foragerCol = "black"
predatorCol = c("gray20", "red3")
conspecificCol = "gray50"
destinationCol = "gray10"

# states
searching = 1 
feeding = 2 
singleState = 3 
notInitialized = 4


plotImage = function(data, foragerTrack, conspecificTracks, states, predatorLocations, colors, zlim, dimension, iterationCounter)
{
	# dimension is minX, minY, maxX, maxY
	xVals = seq(from = dimension[1], to = dimension[3], length = nrow(data) + 1)
	yVals = seq(from = dimension[2], to = dimension[4], length = ncol(data) + 1)
	image(data, x = xVals, y = yVals, zlim = zlim, axes = FALSE, col = colors, asp = 1,
		xlab = "", ylab = "")
		
	if (!is.null(conspecificTracks))
	{			
		for (id in unique(conspecificTracks[,1]))	
		{
			lines(x = conspecificTracks[conspecificTracks[,1] == id,2], conspecificTracks[conspecificTracks[,1] ==id,3], col = conspecificCol)
		}
	}
	points(x = foragerTrack[,2], foragerTrack[,3], pch = foragerPch[states], type = "o", col = foragerCol, cex = 1.5)
	points(x = foragerTrack[iterationCounter + 1,2], y = foragerTrack[iterationCounter + 1,3], pch = endPch, col = foragerCol, cex = 1.5)
	if (!is.null(predatorLocations))
	{
		encounters = predatorLocations[,3]
		points(x = predatorLocations[,1], predatorLocations[,2], pch = predatorPch, type = "p", col = predatorCol[encounters + 1], cex = 1.5 + encounters)
	}
	title( paste0("time = ", iterationCounter))
}


addDestination = function(destination)
{
	if (!is.null(destination))
	{
		points(x = destination[1], y = destination[2], pch = destinationPch, type = "p", col = destinationCol, cex = 2)
	}
}

plotProbabilityDistribution = function(probabilities, angle, lim)
{
	if (!is.null(probabilities))
	{
		n = length(probabilities)
		# lim = min( 10 / n) #, 1.1 * max(probabilities))
		# print(lim)
		circle = seq(from = 0, to = 2*pi, length = 200)
		sectors = seq(0, 2*pi, by=2*pi/24)
		angles = seq(0, 2*pi, length = n + 1)[1:n] # to avoid duplicating 0 and 2pi
		uniform = rep(1 /n, n)
		
		plot(0.95 * lim * cos(circle), 0.95 * lim * sin(circle), xlim=c(-lim, lim), ylim=c(-lim, lim),
			 type="l", asp = 1, axes=FALSE, xlab="", ylab="")
		segments(0.9 * lim * cos(sectors), 0.9 * lim * sin(sectors), lim * cos(sectors), lim * sin(sectors))
		
		lines(probabilities*cos(angles), probabilities*sin(angles), col="darkred", lwd=1.5)
		lines(c(0, lim*cos(angle)), c(0, lim*sin(angle)), col = "red")
		lines(uniform*cos(angles), uniform*sin(angles), lty=2)
	}
	else
	{
		plot(1, 1, type = "n")
	}
}

plotResource = function(resourceData, foragerTrack, conspecificTracks, states, predatorLocations, maxResource, dimension, iterationCounter)
{
	pdf(paste("output/plot", sprintf("%06d",iterationCounter), ".pdf", sep = ""), width = 8, height = 8)
	plotImage(resourceData, foragerTrack, conspecificTracks, states, predatorLocations, habitatCols, c(0, maxResource), dimension, iterationCounter)
	dev.off()
}

plotResourceAndMemory = function(resourceData, memoryData, foragerTrack, conspecificTracks, states, memoryUsage, destination, predatorLocations, maxResource, dimension, iterationCounter)
{
	pdf(paste("output/plot", sprintf("%06d",iterationCounter), ".pdf", sep = ""), width = 16, height = 8)
	par(mfrow = c(1,2))
	plotImage(resourceData, foragerTrack, conspecificTracks, states, predatorLocations, habitatCols, c(0, maxResource), dimension, iterationCounter)
	addDestination(destination)
	plotImage(memoryData, foragerTrack, conspecificTracks, states, predatorLocations, memoryCols, c(-maxResource, maxResource), dimension, iterationCounter)
	addDestination(destination)
	dev.off()
}

plotResourceAndMemoryAndProbability = function(resourceData, memoryData, resourceProbabilities, predatorProbabilities, aggregateProbabilities, 
	foragerTrack, conspecificTracks, states, memoryUsage, destination, angle, emd, predatorLocations, maxResource, dimension, iterationCounter)
{
	pdf("output/plot_test.pdf", width = 12, height = 10)
	dev.off()
	pdf(paste("output/plot", sprintf("%06d",iterationCounter), ".pdf", sep = ""), width = 12, height = 10)
	layout(matrix(c(rep(1:2, each = 3, length = 18), rep(3:5, each = 2, length = 12)), byrow = TRUE, nrow = 5, ncol = 6))
	plotImage(resourceData, foragerTrack, conspecificTracks, states, predatorLocations, habitatCols, c(0, maxResource), dimension, iterationCounter)
	addDestination(destination)
	plotImage(memoryData, foragerTrack, conspecificTracks, states, predatorLocations, memoryCols, c(-maxResource, maxResource), dimension, iterationCounter)
	addDestination(destination)
	plotProbabilityDistribution(resourceProbabilities, angle, 10 / length(resourceProbabilities))
	title("Resource probability")
	plotProbabilityDistribution(predatorProbabilities, angle, 1)
	title("Predator/conspecific safety")
	plotProbabilityDistribution(aggregateProbabilities, angle, 10 / length(aggregateProbabilities))
	title(paste("Aggregate, EMD =", round(emd, digits = 2)))
	dev.off()
}

returnNumber = function(number)
{
	return(number)
}




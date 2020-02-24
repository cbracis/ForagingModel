matrixBarPlot<-function(x, y, z, xlab, ylab, legend = TRUE, includeConfBars = TRUE, quantiles = c(0.1, 0.9), confBarWidth = 0.05, ...)
{
	#barplot where x is the independent on the x-axis, y is the 
	#dependent on the y-axis and z is the independent given by 
	#different colored bars, see http://rstatistics.tumblr.com/post/470327991/make-a-barplot-with-errorbars-now-this-is-a
	medians = tapply(y, list(z, x), mean)
	quantiles = sort(quantiles)
	qBottom = tapply(y, list(z, x), function(p) quantile(p, quantiles[1]))
	qTop = tapply(y, list(z, x), function(p) quantile(p, quantiles[2]))
	ylims = if(includeConfBars) { c(0, max(qTop, na.rm = TRUE)) } else { c(0, max(medians, na.rm = TRUE)) }
	
	barx = barplot(medians, beside = TRUE, ylim = ylims, xlab = xlab, ylab = ylab, legend.text = legend, ...)
	
	# 95% conf
	if (includeConfBars)
	{
		arrows(barx, qBottom, barx, qTop, angle = 90, code = 3, length = confBarWidth)
	}
	
}

error.bar <- function(x, y, upper, lower = upper, length = 0.1, ...)
{
	# from http://monkeysuncle.stanford.edu/?p=485
	if (length(x) != length(y) | length(y) !=length(lower) | length(lower) != length(upper))
		stop("vectors must be same length")
	arrows(x, y + upper, x, y - lower, angle = 90, code=3, length = length, ...)
}

multiWhich = function(sourceValues, testValues)
{
	return( sapply(testValues, function(x) which(x == sourceValues)) )
}

getColors = function(alpha = 0.5)
{
	alpha = alpha  * 255
	return( c(rgb(76, 0, 92, maxColorValue=255, alpha = alpha),
			  rgb(255, 80, 5, maxColorValue=255, alpha = alpha),
			  rgb(94, 241, 242, maxColorValue=255, alpha = alpha),
			  rgb(128, 128, 128, maxColorValue=255, alpha = alpha),
			  rgb(43, 206, 72, maxColorValue=255, alpha = alpha),
			  rgb(255, 168, 187, maxColorValue=255, alpha = alpha),
			  rgb(0, 117, 220, maxColorValue=255, alpha = alpha),
			  rgb(255, 0, 16, maxColorValue=255, alpha = alpha),
			  rgb(116, 10, 255, maxColorValue=255, alpha = alpha),
			  rgb(204, 204, 0, maxColorValue=255, alpha = alpha),
			  rgb(255, 164, 5, maxColorValue=255, alpha = alpha),
			  rgb(194, 0, 136, maxColorValue=255, alpha = alpha),
			  rgb(51, 102, 0, maxColorValue=255, alpha = alpha),
			  rgb(0, 102, 102, maxColorValue=255, alpha = alpha),
			  rgb(153, 0, 0, maxColorValue=255, alpha = alpha),
			  rgb(204, 255, 153, maxColorValue=255, alpha = alpha)))
	
	#http:#stackoverflow.com/questions/470690/how-to-automatically-generate-n-distinct-colors
	# 	return(c( col2rgb("#FFFFB300", alpha = TRUE), #Vivid Yellow
	# 			  col2rgb("#FF803E75", alpha = TRUE), #Strong Purple
	# 			  col2rgb("#FFFF6800", alpha = TRUE), #Vivid Orange
	# 			  col2rgb("#FFA6BDD7", alpha = TRUE), #Very Light Blue
	# 			  col2rgb("#FFC10020", alpha = TRUE), #Vivid Red
	# 			  col2rgb("#FFCEA262", alpha = TRUE), #Grayish Yellow
	# 			  col2rgb("#FF817066", alpha = TRUE), #Medium Gray
	# 			  
	# 			  #The following will not be good for people with defective color vision
	# 			  col2rgb("#FF007D34", alpha = TRUE), #Vivid Green
	# 			  col2rgb("#FFF6768E", alpha = TRUE), #Strong Purplish Pink
	# 			  col2rgb("#FF00538A", alpha = TRUE), #Strong Blue
	# 			  col2rgb("#FFFF7A5C", alpha = TRUE), #Strong Yellowish Pink
	# 			  col2rgb("#FF53377A", alpha = TRUE), #Strong Violet
	# 			  col2rgb("#FFFF8E00", alpha = TRUE), #Vivid Orange Yellow
	# 			  col2rgb("#FFB32851", alpha = TRUE), #Strong Purplish Red
	# 			  col2rgb("#FFF4C800", alpha = TRUE), #Vivid Greenish Yellow
	# 			  col2rgb("#FF7F180D", alpha = TRUE), #Strong Reddish Brown
	# 			  col2rgb("#FF93AA00", alpha = TRUE), #Vivid Yellowish Green
	# 			  col2rgb("#FF593315", alpha = TRUE), #Deep Yellowish Brown
	# 			  col2rgb("#FFF13A13", alpha = TRUE), #Vivid Reddish Orange
	# 			  col2rgb("#FF232C16", alpha = TRUE) #Dark Olive Green)
	# 	))
}

# from http://www.magesblog.com/2013/04/how-to-change-alpha-value-of-colours-in.html
alpha = function(col, alpha = 1)
{
	if(missing(col))
		stop("Please provide a vector of colours.")
	apply(sapply(col, col2rgb)/255, 2, 
		  function(x) 
		  	rgb(x[1], x[2], x[3], alpha=alpha))  
}

plotScatter = function(data, xCol, yCol, colCol, pchCol = NULL, xLab = "", yLab = "", colTitle = "", pchTitle = "", title = "")
{
	colValues = as.numeric(as.factor(data[,colCol]))
	pchValues = if (is.null(pchCol)) { 1 } else { as.numeric(as.factor(data[,pchCol])) }
	cols = getColors(alpha = 0.5)[1:length(unique(colValues))]

	plot(data[,xCol], data[,yCol], col = cols[colValues], pch = pchValues,
		 xlab = xLab, ylab = yLab)
	legend("topright", col = cols, legend = levels(as.factor(data[,colCol])), pch = 1, title = colTitle)
	if (!is.null(pchCol))
	{
		legend("bottomleft", col = 1, pch = 1:length(unique(pchValues)), legend = levels(as.factor(data[,pchCol])), title = pchTitle)
	}
	title(title)
}

makeLetter = function( letter, where = "topleft", xOffset = 0.08, yOffset = 0.1, cex = 1.0, ... )
{
	if ( where == "above" )
	{
		p = par('usr')
		x = p[1] - xOffset * diff(p[1:2])
		y = p[4] + yOffset * diff(p[3:4])
		text( x = x, y = y, letter, cex = cex, xpd = TRUE, ... )
	}
	else
	{
		legend( where, pt.cex = 0, bty = "n", title = letter, cex = cex, legend = NA, ... )
	}
}

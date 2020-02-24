# Supporting code for landscape generation, etc.

# Where is landscale generation?

# Create a list of landscapes that is a subset of the landExp files
# with parameters m = all and s = 5
landscapes = read.csv("landParametersExp.csv", header = TRUE)
landscapes = subset(landscapes, scale == 5)
files = paste0("land", landscapes$id, ".csv")
length(files)
paramstring = paste(files, collapse = ";")

# helper functions to create and plan simulations

summary(landscapes)
table(landscapes$mean, landscapes$scale)

length(unique(paste(landscapes$mean, landscapes$scale)))
length(paste(landscapes$mean[1:120], landscapes$scale[1:120]))
# first 120 landscapes are all param combos, look and pick subset

par(mfrow = c(4, 4), mar = c(0, 0, 1.5, 0) + 0.1, cex = 0.9, mgp = c(2, 0.5, 0))
habitatCols = c("#FFFFFF", colorRampPalette(c("gray95", "darkolivegreen1"))(20), 
				colorRampPalette(c("darkolivegreen1", "darkolivegreen3"))(21)[-1], 
				colorRampPalette(c("darkolivegreen3", "darkolivegreen"))(21)[-1], "#006400") # dark green
habitatBreaks = c(-1, 1e-10, seq(0, 0.01, length = 21)[-1], seq(0.01, 0.05, length = 21)[-1], seq(0.05, 0.2, length = 21)[-1], 1)
for (i in 1:120)
{
	land = as.matrix(read.csv(paste0("model/land/land", landscapes$id[i], ".csv"), header = TRUE))
	image(land, zlim = c(0, 0.1), col = habitatCols, breaks = habitatBreaks, axes = FALSE)
	title( paste("m =", landscapes$mean[i], "s =", landscapes$scale[i]))
}

# pick mean = -1.5, -1, -0.5, 0, 1 and scale = 2, 5, 10, 25
subland = subset(landscapes, mean %in% c(-1.5, -1, -0.5, 0, 1) & scale %in% c(2, 10))
subland = subset(landscapes, mean %in% c(-1.5, -0.5, 1) & scale %in% c(2, 10))
table(subland$mean, subland$scale)
files = paste0("land", subland$id, ".csv")
paramstring = paste(files, collapse = ";")


subland1 = subland[1:(nrow(subland)/2),]
subland2 = subland[-(1:(nrow(subland)/2)),]
table(subland1$mean, subland1$scale)
table(subland2$mean, subland2$scale)
files1 = paste0("land", subland1$id, ".csv")
files2 = paste0("land", subland2$id, ".csv")
paramstring1 = paste(files1, collapse = ";")
paramstring2 = paste(files2, collapse = ";")

sublandsmall = subset(landscapes, mean %in% c(-1.5, 1) & scale %in% c(2, 10))[1:40,]
table(sublandsmall$mean, sublandsmall$scale)
filessmall = paste0("land", sublandsmall$id, ".csv")
paramstringsmall = paste(filessmall, collapse = ";")




howManyHours = function(nParamCombos, millisPerSim)
{
	return(nParamCombos * millisPerSim / 1000 /60 / 60)
}

howManyHours(200 * 4 * 3 * 3 * 3 * 3 * 5 * 3, 2848)
howManyHours(200 * 4 * 3 * 3 * 3 * 3 * 5 * 3, 3384)

howManyHours(100 * 4 * 3 * 3 * 3 * 3, 2848)
howManyHours(100 * 4 * 3 * 3 * 3 * 3, 3384)

howManyHours(200 * 3 * 3 * 3 * 5 * 5, 3384)


# kinesis more persistances
howManyHours(100 * 9 * 9 * 9 * 3, 300)

# food safety tradeoff
howManyHours(100 * 3 * 3 * 3 * 5, 3500)

# pred test
howManyHours(100 * 4 * 4 * 3 * 3, 3000)

# no predators
howManyHours(200 * 8 * 12 * 4 * 3, 1500) / 24

# new pred
howManyHours(200 * 3 * 3 * 3, 2000)

# time step comparison
howManyHours(40 * 3 * 3, 100 * 3384) +
	howManyHours(40 * 3 * 3, 10 * 3384) +
	howManyHours(40 * 3 * 3, 2 * 3384) +
	howManyHours(40 * 3 * 3, 1 * 3384) +
	howManyHours(40 * 3 * 3, 0.5 * 3384)

# new foraging results with smaller dt, 6 instead of 10 land params
howManyHours(20*6 * 3 * 4 * 6 * 6, 7618)

# write list of movie files
writeFileNames = function(outputFile, directory, filePrefix, fileSuffix, nFiles, digits)
{
	nums = sprintf(paste0("%0", digits, "d"), 0:nFiles)
	files = paste0( directory, "/", filePrefix, nums, fileSuffix)
	filestring = paste(files, collapse = '\n')
	cat(filestring, file = outputFile, sep = "\n")
}

writeFileNames("model/outputmoviefilenames.txt", "output/predatorEternity", "plot", ".pdf", 999, 6)
			 
#!/bin/sh

JARFILE=distrib/ForagingModel.jar

if [ -f $JARFILE ]
then
rm $JARFILE
fi

jar cf $JARFILE -C bin .

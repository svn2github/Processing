#!/bin/sh


#SHORT_REVISION=`head -1 ../../todo.txt | cut -c 2-4`
REVISION=`head -1 ../../todo.txt | cut -c 1-4`
# as of 100, just use 0100 everywhere to avoid confusion
SHORT_REVISION=$REVISION

VERSIONED=`cat ../../app/src/processing/app/Base.java | grep $REVISION`
if [ -z "$VERSIONED" ]
then
  echo Fix the revision number in Base.java
  exit
fi

./make.sh

echo Creating P5 distribution for revision $REVISION...

# remove any old boogers
rm -rf processing 
rm -rf Processing*
rm -rf processing-*

mkdir processing
cp -r ../shared/lib processing/
cp -r ../shared/libraries processing/
cp ../../app/lib/antlr.jar processing/lib/
cp ../../app/lib/ecj.jar processing/lib/
cp ../../app/lib/jna.jar processing/lib/
cp ../shared/revisions.txt processing/

echo Extracting examples...
unzip -q -d processing/ ../shared/examples.zip

echo Extracting reference...
unzip -q -d processing/ ../shared/reference.zip

# add the libraries folder with source
cp -r ../../net processing/libraries/
cp -r ../../opengl processing/libraries/
cp -r ../../serial processing/libraries/
cp -r ../../video processing/libraries/
cp -r ../../pdf processing/libraries/
cp -r ../../dxf processing/libraries/
cp -r ../../xml processing/libraries/
cp -r ../../candy processing/libraries/

# get ds_store file (!)
cp dist/DS_Store processing/.DS_Store

# get package from the dist dir
cp -pR dist/Processing.app processing/
chmod +x processing/Processing.app/Contents/MacOS/JavaApplicationStub

# put jar files into the resource dir, leave the rest in lib
RES=processing/Processing.app/Contents/Resources/Java
mkdir -p $RES
mv processing/lib/*.jar $RES/

# grab pde.jar and export from the working dir
cp work/lib/pde.jar $RES/
cp work/lib/core.jar processing/lib/

# get platform-specific goodies from the dist dir
chmod a+x processing/Processing.app/Contents/MacOS/JavaApplicationStub

# remove boogers
find processing -name "*~" -exec rm -f {} ';'
# need to leave ds store stuff cuz one of those is important
#find processing -name ".DS_Store" -exec rm -f {} ';'
find processing -name "._*" -exec rm -f {} ';'
find processing -name "Thumbs.db" -exec rm -f {} ';'

# clean out the cvs entries
find processing -name "CVS" -exec rm -rf {} ';' 2> /dev/null
find processing -name ".cvsignore" -exec rm -rf {} ';'
find processing -name ".svn" -exec rm -rf {} 2> /dev/null ';'

mv processing/Processing.app "processing/Processing $SHORT_REVISION.app"

NICE_FOLDER="Processing $SHORT_REVISION"
mv processing "$NICE_FOLDER"
mkdir processing-$REVISION
mv "$NICE_FOLDER" processing-$REVISION/

chmod +x mkdmg
./mkdmg processing-$REVISION
rm -rf processing-$REVISION

echo Done.
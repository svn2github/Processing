#!/bin/sh

REVISION=`head -c 4 ../../../todo.txt`

./make.sh

echo Creating P5 distribution for revision $REVISION...

# remove any old boogers
rm -rf processing
rm -rf processing-*

# use 'shared' files as starting point
cp -r ../shared processing
# something like the following might be better:
# find / -name "*.mp3" -exec rm -f {}\;
# and same for cvsignore
rm -rf processing/CVS
rm -rf processing/lib/CVS
rm -rf processing/fonts/CVS
rm -rf processing/reference/CVS
rm -rf processing/reference/images/CVS
rm -rf processing/sketchbook/CVS
rm -rf processing/sketchbook/default/CVS
rm -f processing/sketchbook/default/.cvsignore

rm -rf processing/sketchbook/examples/CVS
rm -rf processing/sketchbook/examples/form00/CVS
rm -rf processing/sketchbook/examples/form01/CVS
rm -rf processing/sketchbook/examples/form02/CVS
rm -rf processing/sketchbook/examples/form03/CVS
rm -rf processing/sketchbook/examples/form04/CVS
rm -rf processing/sketchbook/examples/motion00/CVS
rm -rf processing/sketchbook/examples/motion01/CVS
rm -rf processing/sketchbook/examples/motion02/CVS
rm -rf processing/sketchbook/examples/response00/CVS
rm -rf processing/sketchbook/examples/response01/CVS
rm -rf processing/sketchbook/examples/response02/CVS
rm -rf processing/sketchbook/examples/response03/CVS
rm -rf processing/sketchbook/examples/response04/CVS
rm -rf processing/sketchbook/examples/response05/CVS
rm -rf processing/sketchbook/examples/response06/CVS
rm -rf processing/sketchbook/examples/response07/CVS

# add java (jre) files
unzip -q -d processing jre.zip

# directories used by the app
mkdir processing/lib/build

# grab pde.jar and export from the working dir
cp work/lib/pde.jar processing/lib/
cp -r work/lib/export processing/lib/
rm -rf processing/lib/export/CVS

# get platform-specific goodies from the dist dir
cp dist/Proce55ing.exe processing/
cp dist/lib/pde_windows.properties processing/lib/

# convert notes.txt to windows LFs
# the 2> is because the app is a little chatty
unix2dos processing/readme.txt 2> /dev/null
unix2dos processing/revisions.txt 2> /dev/null
unix2dos processing/lib/pde.properties 2> /dev/null
unix2dos processing/lib/pde_windows.properties 2> /dev/null

# zip it all up for release
echo Zipping and finishing...
P5=processing-$REVISION
mv processing $P5
zip -rq $P5.zip $P5
# nah, keep the new directory around
#rm -rf $P5

echo Done.
#!/bin/bash

export SVN_BASE=https://thanlwinmyit/svn/krs/DocCharConvert/trunk/DocCharConvert
svn checkout $SVN_BASE
cd DocCharConvert
svn checkout https://thanlwinmyit/svn/krs/LanguageTest/trunk/org.apache.xmlbeans
find . -name .svn -exec rm -rf {} \;
cd -

SVN_REV=`svn info | grep "Last Changed Rev:" | sed 's/Last Changed Rev: //'`
tar -jcvf org.thanlwinsoft.doccharconvert-svn$SVN_REV.tar.bz2 DocCharConvert
rm -rf DocCharConvert


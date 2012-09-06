#!/bin/bash
# Install QFJ in the local maven repository.

function die()
{
    echo "ERROR: $1"
    exit 1
}

function downloadFile()
{
    local URL=$1
    local FN=$2
    if [[ ! -f $FN ]] ; then
        wget $URL/$FN || die "Unable to download $FN from $URL"
    else
        echo "$FN already downloaded"
    fi

}

URL_BASE=http://downloads.sourceforge.net/project/quickfixj/QuickFIX_J
VERSION=1.5.2

BIN_TARBALL=quickfixj-$VERSION-bin.tar.gz
downloadFile "$URL_BASE/$VERSION" $BIN_TARBALL
SRC_TARBALL=quickfixj-$VERSION-src.tar.gz
downloadFile "$URL_BASE/$VERSION" $SRC_TARBALL

tar xzf $BIN_TARBALL || die "Unable to extract files from $BIN_TARBALL"
tar xzf $SRC_TARBALL || die "Unable to extract files from $SRC_TARBALL"

export JAVA_HOME=/opt/jdk1.6
export M2_HOME=/opt/maven

PATH=$M2_HOME/bin:$PATH

which mvn || die "Command 'mvn' not found."

POM_FILE=qfj-pom.xml
JAR_FILE=quickfixj/quickfixj-all-1.5.2.jar
SOURCE_FILE=quickfixj/src.zip

# Installs the QF/J libraries into the local repository.
mvn install:install-file -Dfile=$JAR_FILE -DpomFile=$POM_FILE
(($? == 0))  || die "Unable to install $JAR_FILE"

mvn install:install-file -Dfile=$SOURCE_FILE -DpomFile=$POM_FILE -Dclassifier=sources
(($? == 0)) || die "Unable to install $SOURCE_FILE"


#!/bin/bash

. $(pwd)/release-versions.txt

MESSAGE=$(git log -1 --pretty=%B)

git checkout -- .mvn/maven.config
RELEASE_VERSION=`./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout`
CURRENT_BRANCH=`git rev-parse --abbrev-ref HEAD`

git remote set-branches origin 'gh-pages'
git fetch -v

git checkout gh-pages
mkdir -p ${API_FAMILY}/${RELEASE_VERSION}/htmlsingle
cp target/generated-docs/* ${API_FAMILY}/${RELEASE_VERSION}/htmlsingle
git add ${API_FAMILY}/${RELEASE_VERSION}/

if [[ $RELEASE_VERSION == *[RCM]* ]]
then
  DOC_DIR="milestone"
elif [[ $RELEASE_VERSION == *SNAPSHOT* ]]
then
  DOC_DIR="snapshot"
else
  DOC_DIR="stable"
fi

mkdir -p ${API_FAMILY}/${DOC_DIR}/htmlsingle
cp target/generated-docs/* ${API_FAMILY}/${DOC_DIR}/htmlsingle
git add ${API_FAMILY}/${DOC_DIR}/

git commit -m "$MESSAGE"
git push origin gh-pages
git checkout $CURRENT_BRANCH

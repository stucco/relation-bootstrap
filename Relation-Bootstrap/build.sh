#!/bin/sh
mvn --non-recursive scm:checkout -Dmodule.name=entity-extractor -DscmVersion=bootstrap -DscmVersionType=branch
cd entity-extractor
mvn -e clean install -Dmaven.test.skip=true
cd ..
mvn -e clean package -Dmaven.test.skip=true
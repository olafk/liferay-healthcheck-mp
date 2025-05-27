#!/bin/bash

function revert_it_all {
    echo reverting changes
    cp ../../gradle.properties.bak ../../gradle.properties
    mv healthcheck-api/build.gradle.bak healthcheck-api/build.gradle
    mv healthcheck-bestpractice/build.gradle.bak healthcheck-bestpractice/build.gradle
    mv healthcheck-breaking-changes/build.gradle.bak healthcheck-breaking-changes/build.gradle
    mv healthcheck-operation/build.gradle.bak healthcheck-operation/build.gradle
    mv healthcheck-relaxed/build.gradle.bak healthcheck-relaxed/build.gradle
    mv healthcheck-web/build.gradle.bak healthcheck-web/build.gradle
    mv healthcheck-api/bnd.bnd.bak healthcheck-api/bnd.bnd
    mv healthcheck-bestpractice/bnd.bnd.bak healthcheck-bestpractice/bnd.bnd
    mv healthcheck-breaking-changes/bnd.bnd.bak healthcheck-breaking-changes/bnd.bnd
    mv healthcheck-operation/bnd.bnd.bak healthcheck-operation/bnd.bnd
    mv healthcheck-relaxed/bnd.bnd.bak healthcheck-relaxed/bnd.bnd
    mv healthcheck-web/bnd.bnd.bak healthcheck-web/bnd.bnd
}

function build_it_all {
    echo Building $2
    sed -i.bak "s/release.dxp.api/$1/" */build.gradle
    sed -i.bak "s/DXP/$2/" */bnd.bnd
    sed -i.bak "s/liferay.workspace.product/\#liferay.workspace.product/" ../../gradle.properties
    printf "\nliferay.workspace.product=$3" >> ../../gradle.properties
    ../../gradlew clean jar
    if [ -f healthcheck-api/build/libs/*.jar ]; then
        mkdir -p dist/$2
        mv */build/libs/*.jar dist/$2/
        zip -j dist/$2.zip dist/$2/*.jar
    else
        echo "**************************************"
        echo "** Build Problem? Can't find output **"
        echo "**************************************"
        sleep 3
    fi
    echo Reverting files after executing steps for $2
    revert_it_all
    sleep 2
}



build_it_all release.dxp.api DXP-2025-Q2 dxp-2024.q2.0
build_it_all release.dxp.api DXP-2025-Q1 dxp-2025.q1.0-lts
build_it_all release.dxp.api DXP-2024-Q4 dxp-2024.q4.0
build_it_all release.dxp.api DXP-2024-Q3 dxp-2024.q3.0
build_it_all release.dxp.api DXP-2024-Q2 dxp-2024.q2.0
build_it_all release.portal.api CE-GA132 portal-7.4-ga132
build_it_all release.portal.api CE-GA129 portal-7.4-ga129
build_it_all release.portal.api CE-GA125 portal-7.4-ga125
build_it_all release.portal.api CE-GA120 portal-7.4-ga120

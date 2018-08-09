# @dockerfile.Template@
MAINTAINER @project.maintainer@

FROM dr.rbkmoney.com/rbkmoney/service-java:@dockerfile.base.service.tag@ as service-java
FROM dr.rbkmoney.com/rbkmoney/build:@dockerfile.build.tag@ as build

COPY --from=service-java / /tmp/portage-root/
# COPY shared/portage/ /usr/portage
# COPY shared/overlays /var/lib/layman

ENV ROOT=/tmp/portage-root
RUN emerge --sync && emerge fontconfig

FROM dr.rbkmoney.com/rbkmoney/service-java:@dockerfile.base.service.tag@
COPY --from=build /tmp/portage-root/ /

COPY @artifactId@-@version@.jar /opt/@artifactId@/@artifactId@.jar

WORKDIR /opt/@artifactId@
CMD ["java", "-Xmx256m", "-jar","/opt/@artifactId@/@artifactId@.jar"]

EXPOSE @exposed.ports@

LABEL com.rbkmoney.@artifactId@.parent=service-java \
    com.rbkmoney.@artifactId@.parent_tag=@dockerfile.base.service.tag@ \
    com.rbkmoney.@artifactId@.commit_id=@git.commit.id@ \
    com.rbkmoney.@artifactId@.branch=@git.branch@

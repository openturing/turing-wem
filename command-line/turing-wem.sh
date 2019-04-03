#! /bin/bash

PRG="`command type $0 | cut -d' ' -f3-`" >/dev/null 2>&1
J_HOME=`dirname $PRG`/../jre

if [ "$OS" = "Windows_NT" ]
  then SEP=";"
else SEP=":"
fi


BINDIR="`dirname $PRG`"

. $BINDIR/setupcfgenv.sh

CLASSDIR=$BINDIR/../lib

CP=$CLASSDIR
CP=$CP$SEP$CLASSDIR/jsafe.jar
CP=$CP$SEP$CLASSDIR/log4j.jar
CP=$CP$SEP$CLASSDIR/vgn-shared-logging.jar
CP=$CP$SEP$CLASSDIR/vgncommon.jar
CP=$CP$SEP$CLASSDIR/vgnssl.jar
CP=$CP$SEP$CLASSDIR/vgn-appsvcs-combined.jar
CP=$CP$SEP$CLASSDIR/vgn-appsvcs-config.jar
CP=$CP$SEP$CLASSDIR/vgnhpdapi-8.0.jar
CP=$CP$SEP$CLASSDIR/commons-httpclient.jar
CP=$CP$SEP$CLASSDIR/commons-logging.jar
CP=$CP$SEP$CLASSDIR/commons-codec.jar
CP=$CP$SEP$CLASSDIR/commons-dbcp.jar
CP=$CP$SEP$CLASSDIR/castor-0.9.3.9.jar
CP=$CP$SEP$CLASSDIR/sdk/vgn-appsvcs-cda.jar
CP=$CP$SEP$CLASSDIR/apache-httpclient.jar
CP=$CP$SEP$CLASSDIR/apache-httpcore.jar
CP=$CP$SEP$CLASSDIR/apache-httpmime.jar
CP=$CP$SEP$CLASSDIR/jackson-core.jar

CP=$CP$SEP$CLASSDIR/turing-wem-all.jar

JDBCDIR=$BINDIR/../jdbc
CP=$CP$SEP$JDBCDIR/ojdbc6.jar
CP=$CP$SEP$JDBCDIR/ojdbc5.jar
CP=$CP$SEP$JDBCDIR/vgnjdbc.jar
CP=$CP$SEP.
CP=$CP$SEP$BINDIR/.
CP=$CP$SEP$CLASSPATH

exec $JAVAPROG $JAVAARGS -cp $CP -Xmx512m  com.viglet.turing.wem.TurWEMIndexer "$@"




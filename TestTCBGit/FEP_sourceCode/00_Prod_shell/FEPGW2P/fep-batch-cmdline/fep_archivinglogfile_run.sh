#!/bin/sh

/fep/fep-app/fep-batch-cmdline/fep_batch_cmdline_run.sh \
com.syscom.fep.batch.task.cmn.ArchivingLogFile \
"\
/SourceDir:/fep/logs/ \
/TargetDir:/fep/logs/archives \
/ArchiveDay:7 \
/ReserveDay:30 \
/BatchLogPath:/fep/logs \
/CallBatchJob:false \
"

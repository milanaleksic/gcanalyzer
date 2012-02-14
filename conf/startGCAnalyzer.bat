@echo off
cd..
mvn exec:java -Dexec.mainClass="net.milanaleksic.gcanalyzer.GCAnalyzerApplication" -Dexec.args="m:\WebSite\GC\gc_log_apache.log"
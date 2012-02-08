@echo off
java -cp .\lib\*;%GROOVY_HOME%\embeddable\*;.\out\production\GCAnalyzer\ net.milanaleksic.gcanalyzer.GCAnalyzer "resources\gc\gc_log_apache.log"
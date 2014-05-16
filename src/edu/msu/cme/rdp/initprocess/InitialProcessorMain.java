/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.msu.cme.rdp.initprocess;

import edu.msu.cme.pyro.PipelineGene;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 *
 * @author fishjord
 */
public class InitialProcessorMain {

    private static final Options cmdOptions = new Options();

    static {
        cmdOptions.addOption("f", "forward-primers", true, "Comma seperated list of forward primers");
        cmdOptions.addOption("r", "reverse-primers", true, "Comma seperated list of reverse primers (default=null)");
        cmdOptions.addOption("g", "gene-name", true, "Gene name, possible values are RRNA16S and OTHER (default=RRNA16S)");
        cmdOptions.addOption("m", "min-length", true, "Minimum sequence length after primer triming (default=0)");
        cmdOptions.addOption("x", "max-length", true, "Maximum sequence length after primer triming (default=1000)");
        cmdOptions.addOption("Q", "min-qual", true, "Minimum sequence length after primer triming (default=20)");
        cmdOptions.addOption("n", "max-ns", true, "Maxmimum number of Ns allowed in a sequence (default=0)");
        cmdOptions.addOption("q", "qual-file", true, "Quality input file (default=null)");
        cmdOptions.addOption("F", "max-forward", true, "Maximum forward edit distance (default=2)");
        cmdOptions.addOption("R", "max-reverse", true, "Maximum reverse edit distance (default=0)");
        cmdOptions.addOption("s", "seq-file", true, "Sequence file to process");
        cmdOptions.addOption("S", "skip-notag", false, "Don't process no tag sequences");
        cmdOptions.addOption("o", "outdir", true, "Output directory (default=cwd)");
        cmdOptions.addOption("t", "tag-file", true, "Tag file (default=null)");
        cmdOptions.addOption("O", "result-dir-name", true, "Result dir name (default=result_dir)");
        cmdOptions.addOption("p", "keep-primer", true, "Don't trim primers (default=false)");
    }


    public static void main(String[] args) throws Exception {
        File tagFile = null;
        File outDir = new File(".");
        InitialProcessOptions options = new InitialProcessOptions();
        options.fPrimer = null;
        options.rPrimer = null;
        options.genename = PipelineGene.RRNA_16S_BACTERIA;
        options.minSeqLength = 0;
        options.maxSeqLength = 1000;
        options.noofns = 0;
        options.qualInfile = null;
        options.forwardMaxEditDist = 2;
        options.reverseMaxEditDist = 0;
        options.seqInfile = null;
        options.keepPrimers = false;
        options.minExpQualScore = 20;
        String resultDirName = "result_dir";

        try {
            CommandLine line = new PosixParser().parse(cmdOptions, args);

            if(line.hasOption("seq-file")) {
                options.seqInfile = Arrays.asList(new File(line.getOptionValue("seq-file")));
            } else {
                throw new Exception("seq-file is required");
            }

            if(line.hasOption("min-qual")) {
                options.minExpQualScore = Integer.parseInt(line.getOptionValue("min-qual"));
            }

            if(line.hasOption("keep-primer")) {
                try {
                    options.keepPrimers = Boolean.parseBoolean(line.getOptionValue("keep-primer"));
                } catch(Exception e) {
                    throw new Exception("keep-primer must be true/false");
                }
            }

            if(line.hasOption("tag-file")) {
                tagFile = new File(line.getOptionValue("tag-file"));
            } else {
                throw new Exception("tag-file is required");
            }

            if(line.hasOption("outdir")) {
                outDir = new File(line.getOptionValue("outdir"));
                if(!outDir.exists()) {
                    if(!outDir.mkdir()) {
                        throw new Exception("Output directory \"" + outDir.getAbsolutePath() + "\" doesn't exist and could not be created");
                    }
                }
                if(!outDir.isDirectory()) {
                    throw new Exception("Output directory must be a directory");
                }
            }

            if(line.hasOption("gene-name")) {
                try {
                    options.genename = PipelineGene.valueOf(line.getOptionValue("gene-name"));
                } catch(Exception e) {
                    throw new Exception("Gene name must be one of " + Arrays.asList(PipelineGene.values()));
                }
            }

            if(line.hasOption("min-length")) {
                options.minSeqLength = new Integer(line.getOptionValue("min-length"));
            }
            
            if(line.hasOption("max-length")) {
                options.maxSeqLength = new Integer(line.getOptionValue("max-length"));
            }

            if(line.hasOption("max-ns")) {
                options.noofns = new Integer(line.getOptionValue("max-ns"));
            }

            if(line.hasOption("max-forward")) {
                options.forwardMaxEditDist = new Integer(line.getOptionValue("max-forward"));
            }

            if(line.hasOption("max-reverse")) {
                options.reverseMaxEditDist = new Integer(line.getOptionValue("max-reverse"));
            }

            if(line.hasOption("result-dir-name")) {
                resultDirName = line.getOptionValue("result-dir-name");
            }

            if(line.hasOption("forward-primers")) {
                options.fPrimer = Arrays.asList(line.getOptionValue("forward-primers").split(","));
            } else {
                options.fPrimer = new ArrayList();
            }

            if(line.hasOption("reverse-primers")) {
                options.rPrimer = Arrays.asList(line.getOptionValue("reverse-primers").split(","));
            } else {
                options.rPrimer = new ArrayList();
            }

            if(line.hasOption("skip-notag")) {
                options.processNoTag = false;
            }

        } catch(Exception e) {
            System.err.println("Error: " + e.getMessage());
            new HelpFormatter().printHelp("USAGE: InitialProcessorMain <options>", cmdOptions);
            return;
        }

        File resultDir = new File(outDir, resultDirName);
        File tagSortDir = new File(outDir, "tag_sort");
        InitialProcessor.doInitialProcessing(resultDir, tagSortDir, tagFile, options);
    }
}

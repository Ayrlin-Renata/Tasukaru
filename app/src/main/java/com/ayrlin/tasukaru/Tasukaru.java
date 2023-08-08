/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.ayrlin.tasukaru;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import co.casterlabs.caffeinated.pluginsdk.*;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@CaffeinatedPluginImplementation
public class Tasukaru extends CaffeinatedPlugin {

    private FastLogger log;
    private TListener tlist;
    private VBHandler vb;

    public Tasukaru() {
        super();
        log = this.getLogger();
    }

    @Override
    public void onInit() {
        log.debug("Tasukaru onInit()");
        log.info("Hello World!");

        // file setup
        String tDir;
        try {
            tDir = initPluginDir("tasukaru");
        } catch (IOException e) {
            log.severe("Tasukaru unable to initialize plugin directory. Init aborted.");
            e.printStackTrace();
            return;
        }

        // database init
        vb = new VBHandler(log, tDir + "/");
        vb.run();

        // listener init
        tlist = new TListener(log, vb);
        addKoiListener(tlist);
    }

    @Override
    public void onClose() {
        log.debug("Tasukaru onClose()");
        log.info("Goodbye World!");
    }

    private String initPluginDir(String plugin) throws IOException {
        String wDir = System.getProperty("user.dir");
        log.debug("Working Directory reported as: " + wDir);
        Path wPath = Paths.get(wDir);
        Path cPath = wPath.getParent();
        String cDir;
        try {
            cDir = cPath.toRealPath().toString();
        } catch (IOException e) {
            log.severe("Plugin unable to initialize properly due to inability to: find casterlabs base path.");
            throw e;
        }
        log.debug("Casterlabs-Caffeinated directory reported as: " + cDir);
        String pDir = cDir + "/plugins";
        Path pPath = Paths.get(pDir);
        String tDir;
        try {
            tDir = pPath.toRealPath().toString() + "/" + plugin;
        } catch (IOException e) {
            log.severe(
                    "Plugin unable to initialize properly due to inability to: find plugin directory path.");
            throw e;
        }
        Path tPath = Paths.get(tDir);

        if (!Files.isDirectory(tPath)) {
            log.warn("Unable to find " + plugin + " plugin directory: " + tDir);
            // create folder
            log.info("Creating " + plugin + " plugin directory: " + tDir);
            try {
                Files.createDirectories(tPath);
            } catch (IOException e) {
                log.severe(
                        "Plugin unable to initialize properly due to inability to: create " + plugin + " directory: "
                                + tDir);
                throw e;
            }
        }
        return tDir;
    }

    @Override
    public String getName() {
        return "Tasukaru";
    }

    @Override
    public String getId() {
        return "com.ayrlin.tasukaru";
    }

}
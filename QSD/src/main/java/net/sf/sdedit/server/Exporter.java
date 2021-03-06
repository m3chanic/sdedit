// Copyright (c) 2006 - 2016, Markus Strauch.
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// 
// * Redistributions of source code must retain the above copyright notice, 
// this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright notice, 
// this list of conditions and the following disclaimer in the documentation 
// and/or other materials provided with the distribution.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
// THE POSSIBILITY OF SUCH DAMAGE.

package net.sf.sdedit.server;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import net.sf.sdedit.ui.G2DGraphicsDevice;
import net.sf.sdedit.util.Utilities;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.ImageGraphics2D;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.freehep.graphicsio.gif.GIFGraphics2D;
import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.freehep.graphicsio.ps.PSGraphics2D;
import org.freehep.graphicsio.svg.SVGGraphics2D;


/**
 * An Exporter is a paint device that can redirect its output to an output
 * stream, using one of various output formats.
 * 
 * @author Markus Strauch
 */
public class Exporter extends G2DGraphicsDevice {
	
    private OutputStream stream;

    private String type;

    private VectorGraphics vg;

    private String orientation;

    private String format;

    private Dimension dim;

    public Exporter(String orientation, String format) {
        super();
        this.orientation = orientation;
        this.format = format;
    }
    
    /**
     * Returns an Exporter object if the exporting library is available,
     * otherwise <tt>null</tt>. The following output types are supported:
     * 
     * ps, pdf, swf, emf, svg, png, gif, jpg, bmp
     * 
     * @param type
     *            describes the output format
     * @param orientation
     *            one of {Portrait,Landscape}
     * @param stream
     *            the stream to redirect the output to
     * @return an instance of an Exporter
     */
    public static Exporter getExporter(String type, String orientation, String format, OutputStream stream) {
        Exporter exporter = new Exporter(orientation, format);
        exporter.setType(type);
        exporter.setOutputStream(stream);
        return exporter;
    }

    private VectorGraphics getGraphics() {
        VectorGraphics vectorGraphics;
        if (orientation == null) {
            orientation = dim.getWidth() <= dim.getHeight() ? "Portrait"
                    : "Landscape";
        }
        if (type.equals("gif")) {
            vectorGraphics = new GIFGraphics2D(stream, dim);
        } else if (type.equals("png")) {
            vectorGraphics = new ImageGraphics2D(stream, dim, "png");
        } else if (type.equals("bmp")) {
            vectorGraphics = new ImageGraphics2D(stream, dim, "bmp");
        } else if (type.equals("jpg")) {
            vectorGraphics = new ImageGraphics2D(stream, dim, "jpg");
        } else if (type.equals("pdf")) {
            PDFGraphics2D pdf = new PDFGraphics2D(stream, dim);
            Properties properties = new Properties();
            properties.setProperty(PDFGraphics2D.ORIENTATION, orientation);
            properties.setProperty(PDFGraphics2D.PAGE_SIZE, format);
            pdf.setProperties(properties);
            vectorGraphics = pdf;
        } else if (Utilities.in(type, "ps", "eps")) {
            PSGraphics2D ps = new PSGraphics2D(stream, dim);
            Properties properties = new Properties();
            properties.setProperty(PSGraphics2D.ORIENTATION, orientation);
            properties.setProperty(PSGraphics2D.PAGE_SIZE, format);
            ps.setProperties(properties);
            ps.setMultiPage("ps".equals(type));
            vectorGraphics = ps;
        } else if (type.equals("emf")) {
            vectorGraphics = new EMFGraphics2D(stream, dim);
        } else if (type.equals("svg")) {
            vectorGraphics = new SVGGraphics2D(stream, dim);
        } else {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
        return vectorGraphics;
    }

    @Override
    protected Graphics2D createDummyGraphics() {
        dim = new Dimension(1, 1);
        VectorGraphics dummy = getGraphics();
        Font font = getFont();
        dummy.setFont(font);
        return dummy;
    }

    @Override
    protected Graphics2D createGraphics() {
        dim = new Dimension(getWidth(), getHeight());
        vg = getGraphics();
        if (vg instanceof ImageGraphics2D) {
            vg.setColor(Color.WHITE);
            vg.fillRect(0, 0, getWidth(), getHeight());
        }
        return vg;
    }

    protected void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    protected void setType(String type) {
        this.type = type.toLowerCase();
    }

    public void export() {
        vg.startExport();
        if (vg instanceof PSGraphics2D && "ps".equals(type)) {
            try {
                ((PSGraphics2D) vg).openPage(dim, "");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        drawAll();
        if (vg instanceof PSGraphics2D && "ps".equals(type)) {
            ((PSGraphics2D) vg).closePage();
        }
        vg.endExport();
    }

}

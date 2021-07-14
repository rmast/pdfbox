/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.tools;

import org.apache.pdfbox.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.util.Iterator;

/**
 * Test suite for ExtractImages.
 * should this test check for the availability of the JAI-imageio-libraries before failing?
 */
public class TestExtractImages
{
    private static final File OUT_DIR = new File("target/test-output");

    @BeforeClass
    public static void init() throws Exception
    {
        OUT_DIR.mkdirs();
    }

    /**
     * Run the text extraction test using a pdf with embedded pdfs.
     *
     * @throws Exception if something went wrong
     */
    @Test
    public void testEmbeddedPDFs() throws Exception
    {
        File tempPdfFile = new File(OUT_DIR, "testExportTiffPictureDensity.pdf");
        File resultPicture1 = new File(OUT_DIR, "testExportTiffPictureDensity-1.jpg");
        File resultPicture2 = new File(OUT_DIR, "testExportTiffPictureDensity-2.tiff");
        File resultPicture3 = new File(OUT_DIR, "testExportTiffPictureDensity-3.tiff");
        File resultPicture4 = new File(OUT_DIR, "testExportTiffPictureDensity-4.tiff");
        File resultPicture5 = new File(OUT_DIR, "testExportTiffPictureDensity-5.tiff");
        File source = new File("src/test/resources/org/apache/pdfbox/testExportTiffPictureDensity.pdf");
        FileInputStream inputStream = new FileInputStream(source);
        //target file declaration
        FileOutputStream outputStream = new FileOutputStream(tempPdfFile);

        IOUtils.copy(inputStream, outputStream);
        outputStream.close();
        inputStream.close();

        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(outBytes));
        try
        {
            ExtractImages.main(new String[]{tempPdfFile.getAbsolutePath(), "-includeDensity"});
        }
        finally
        {
            // Restore stdout
            System.setOut(stdout);

        }

        assert(
                CheckFileDensity(resultPicture1)+
                        CheckFileDensity(resultPicture2)+
                        CheckFileDensity(resultPicture3)+
                        CheckFileDensity(resultPicture4)+
                        CheckFileDensity(resultPicture5)
                        == 900);

        tempPdfFile.delete();
        resultPicture1.delete();
        resultPicture2.delete();
        resultPicture3.delete();
        resultPicture4.delete();
        resultPicture5.delete();
    }

    private long CheckFileDensity(File resultPicture) throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(resultPicture);
        Iterator it = ImageIO.getImageReaders(iis);
        if (!it.hasNext())
        {
            System.err.println("No reader for this format");
            return 0;
        }

        ImageReader reader = (ImageReader) it.next();
        reader.setInput(iis);

        IIOMetadata meta = reader.getImageMetadata(0);
        IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree("javax_imageio_1.0");
        NodeList nodes = root.getElementsByTagName("HorizontalPixelSize");
        if (nodes.getLength() > 0)
        {
            IIOMetadataNode dpcWidth = (IIOMetadataNode) nodes.item(0);
            NamedNodeMap nnm = dpcWidth.getAttributes();
            Node item = nnm.item(0);
            int xDPI = Math.round(25.4f / Float.parseFloat(item.getNodeValue()));
            return xDPI;
        }
        else
            return 0;
    }
}
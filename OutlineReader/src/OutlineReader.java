
import com.essbase.api.base.*;
import com.essbase.api.session.*;
import com.essbase.api.datasource.*;
import com.essbase.api.domain.*;
import com.essbase.api.metadata.*;
import java.util.Arrays;

/*
 * OutlineReader
 * <p>
 *
 * List outline properties
 *
 * @version 1.4.1
 * @author Sebastien Roux
 * @mail roux.sebastien@gmail.com
 *
 * The MIT License
 * Copyright (c) 2012 Sébastien Roux
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public class OutlineReader {

    private static String essUsr;
    private static String essPwd;
    private static String essSvr;
    private static String essProvider;
    private static String essApp;
    private static String essDb;
    private static boolean indent = false;
    private static String dimension = "";
    private static final int FAILURE_CODE = 1;
    private static String delimiter = "\t";
    static IEssCube cube;

    public static String getEssApp() {
        return essApp;
    }

    public static void setEssApp(String essApp) {
        OutlineReader.essApp = essApp;
    }

    public static String getEssDb() {
        return essDb;
    }

    public static void setEssDb(String essDb) {
        OutlineReader.essDb = essDb;
    }

    public static String getEssPwd() {
        return essPwd;
    }

    public static void setEssPwd(String essPwd) {
        OutlineReader.essPwd = essPwd;
    }

    public static String getEssSvr() {
        return essSvr;
    }

    public static void setEssSvr(String essSvr) {
        OutlineReader.essSvr = essSvr;
    }

    public static String getEssUsr() {
        return essUsr;
    }

    public static void setEssUsr(String essUsr) {
        OutlineReader.essUsr = essUsr;
    }

    public static String getEssProvider() {
        return essProvider;
    }

    public static void setEssProvider(String essProvider) {
        if (essProvider == null) {
            // Default provider (APS) path
            OutlineReader.essProvider = "http://" + getEssSvr() + ":13080/aps/JAPI";
        } else {
            OutlineReader.essProvider = essProvider;
        }
    }

    public static boolean getIndent() {
        return indent;
    }

    public static void setIndent(boolean indent) {
        OutlineReader.indent = indent;
    }

    public static String getDimension() {
        return dimension;
    }

    public static void setDimension(String dimension) {
        OutlineReader.dimension = dimension;
    }

    public static String getDelimiter() {
        return delimiter;
    }

    public static void setDelimiter(String delimiter) {
        OutlineReader.delimiter = delimiter;
    }

    public static void ReadOutline() {
        int statusCode = 0;
        IEssbase ess = null;
        IEssOlapServer olapSvr = null;

        try {

            // Create JAPI instance.
            ess = IEssbase.Home.create(IEssbase.JAPI_VERSION);

            // Sign on to the Provider
            IEssDomain dom = ess.signOn(getEssUsr(), getEssPwd(), false, null, getEssProvider());

            // Open connection with olap server and get the cube.
            olapSvr = (IEssOlapServer) dom.getOlapServer(getEssSvr());
            olapSvr.connect();
            cube = olapSvr.getApplication(getEssApp()).getCube(getEssDb());

            listOutlineMembers(cube);

        } catch (EssException x) {
            System.err.println("Error: " + x.getLocalizedMessage());
            statusCode = FAILURE_CODE;
        } finally {
            try {
                if (olapSvr != null && olapSvr.isConnected() == true) {
                    olapSvr.disconnect();
                }
            } catch (EssException x) {
                System.err.println("Error: " + x.getLocalizedMessage());
            }

            try {
                if (ess != null && ess.isSignedOn() == true) {
                    ess.signOff();
                }
            } catch (EssException x) {
                System.err.println("Error: " + x.getLocalizedMessage());
            }
        }

        // Set status to failure only if exception occurs and do abnormal termination
        // otherwise, it will by default terminate normally
        if (statusCode == FAILURE_CODE) {
            System.exit(FAILURE_CODE);
        }
    }

    static void listOutlineMembers(IEssCube cube) throws EssException {

        IEssCubeOutline otl = null;
        String[] aliasTables = cube.getAliasTableNames();
        String delim = getDelimiter();

        // Alias header
        String aliasHeader = "";

        for (int l = 0; l < aliasTables.length; l++) {

            if (l == 0) {
                aliasHeader = "Alias (" + aliasTables[0] + ")";
            } else {
                aliasHeader = aliasHeader + delim + "Alias (" + aliasTables[l] + ")";
            }
        }

        try {
            otl = cube.openOutline();

            // Header
            System.out.println("Dimension" + delim + "Parent" + delim + "Child" + delim + aliasHeader
                    + delim + "Consolidation" + delim + "Data storage"
                    + delim + "Two Pass" + delim + "Expense reporting" + delim + "# Chidren"
                    + delim + "Formula" + delim + "Solve order" + delim + "Level" + delim + "Generation"
                    + delim + "UDA" + delim + "Attribute" + delim + "Comment");

            IEssIterator dims = otl.getDimensions();

            for (int i = 0; i < dims.getCount(); i++) {
                IEssDimension dim = (IEssDimension) dims.getAt(i);
                listOutlineMembers_helper(dim.getDimensionRootMember());
            }

            otl.close();
            otl = null;
        } catch (EssException x) {
            System.err.println("Error: " + x.getLocalizedMessage());
        } finally {

            if (otl != null) {
                try {
                    otl.close();
                } catch (EssException x) {
                    System.err.println("Error: " + x.getLocalizedMessage());
                }
            }
        }
    }

    private static void listOutlineMembers_helper(IEssMember mbr)
            throws EssException {

        String[] aliasTables = cube.getAliasTableNames();
        String essConso = "";
        String essChild = "";
        String essAlias = "";
        String essFormula = "";
        String essSolveOrder = "";
        String[] essUda;
        String uda = "";
        IEssIterator essAttribute = null;
        String attribute = "";
        String indentation = "";
        String delim = getDelimiter();
        String storage = "";

        //Dimension
        if (getDimension().toLowerCase().equals("") || mbr.getDimensionName().toLowerCase().equals(getDimension().toLowerCase())) {

            //Indentation
            if (getIndent() == true) {

                for (int m = 2; m < mbr.getGenerationNumber(); m++) {

                    if (mbr.getGenerationNumber() >= 2) {
                        indentation = indentation + "    ";
                    }
                }
            }

            //Child
            essChild = indentation + mbr.getName();

            //Alias
            for (int l = 0; l < aliasTables.length; l++) {

                if (l == 0) {
                    if (mbr.getAlias(aliasTables[0]) != null) {
                        essAlias = indentation + mbr.getAlias(aliasTables[0]);
                    }
                } else {
                    if (mbr.getAlias(aliasTables[l]) != null) {
                        essAlias = essAlias + delim + indentation + mbr.getAlias(aliasTables[l]);
                    }
                }
            }

            //Consolidation
            if (mbr.getConsolidationType() != null) {
                essConso = mbr.getConsolidationType().stringValue().substring(0, 1);
            }

            // Data storage
            switch (mbr.getShareOption().intValue()) {
                case 1:
                    storage = "Never share";
                    break;
                case 2:
                    storage = "Label only";
                    break;
                case 3:
                    storage = "Shared member";
                    break;
                case 4:
                    storage = "Dynamic calc and store";
                    break;
                case 5:
                    storage = "Dynamic calc";
                    break;
                default:
                    storage = "Store data";
                    break;
            }

            //Formula
            if (mbr.getFormula() != null) {
                //essFormula = mbr.getFormula();
                essFormula = mbr.getFormula().replaceAll("[\r\n\t]", " ");

                if (cube.getCubeType().stringValue().equals("ASO")) {
                    essSolveOrder = String.valueOf(mbr.getSolveOrder());
                }
            }

            //UDA
            essUda = mbr.getUDAs();

            //Sort asc
            Arrays.sort(essUda);

            if (essUda.length > 0) {

                for (int j = 0; j < essUda.length; j++) {

                    if (j == 0) {
                        uda = essUda[0];
                    } else {
                        uda = uda + ", " + essUda[j];
                    }
                }
            }

            //Attributes
            if (mbr.isAttributesAssociated() == true) {

                essAttribute = mbr.getAssociatedAttributes();

                for (int i = 0; i < essAttribute.getCount(); i++) {

                    IEssBaseObject attrib = (IEssBaseObject) essAttribute.getAt(i);

                    if (i == 0) {
                        attribute = attrib.toString();
                    } else {
                        attribute = attribute + ", " + attrib.toString();
                    }
                }
            }

            //Output
            System.out.println(mbr.getDimensionName() + delim + mbr.getParent()
                    + delim + essChild + delim + essAlias + delim + essConso + delim + storage
                    + delim + mbr.isTwoPassCalculationMember() + delim + mbr.isExpenseMember() + delim
                    + mbr.getChildCount() + delim + essFormula + delim + essSolveOrder + delim
                    + mbr.getLevelNumber() + delim + mbr.getGenerationNumber()
                    + delim + uda + delim + attribute + delim + mbr.getMemberComment());

            boolean fetchAllProps = false;
            IEssIterator mbrs = mbr.getChildMembers(fetchAllProps);

            for (int i = 0; i < mbrs.getCount(); i++) {
                listOutlineMembers_helper((IEssMember) mbrs.getAt(i));
            }
        }
    }
}

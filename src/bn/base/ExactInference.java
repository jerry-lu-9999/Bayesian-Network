package bn.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import bn.core.Assignment;
import bn.core.BayesianNetwork;
import bn.core.Distribution;
import bn.core.Inferencer;
import bn.core.RandomVariable;
import bn.core.Value;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;

public class ExactInference implements Inferencer {

    @Override
    public Distribution query(RandomVariable X, Assignment e, BayesianNetwork network) {
        return null;
    }

    public static Distribution enumerationAsk(RandomVariable X, Assignment e, BayesianNetwork network){
        Distribution q = new bn.base.Distribution(X);
        for (Value v : X.getDomain()) {
            Assignment eCopy = e.copy();
            eCopy.put(X, v);
            q.put(v, enumerateAll(network.getVariablesSortedTopologically(), eCopy, network));
        }
        q.normalize();
        return q;  
    }

    public static double enumerateAll(List<RandomVariable>vars, Assignment e, BayesianNetwork bn){
        if (vars.isEmpty()){
            return 1.0;
        }
        RandomVariable Y = vars.get(0);
        if (e.containsKey(Y)){
            //System.out.println("contains loop:" + bn.getProbability(Y, e));
            //Assignment eCopy = e.copy();
            return bn.getProbability(Y, e) *enumerateAll(rest(vars), e, bn);
        } else{
            double ysum = 0.0;
            Assignment eCopy = e.copy();
            for (Value v : Y.getDomain()){
                eCopy.put(Y, v);
                //System.out.println("else loop: " + bn.getProbability(Y, eCopy));
                ysum += bn.getProbability(Y, eCopy) * enumerateAll(rest(vars),eCopy, bn);
            }
            return ysum;
        }
    }

    public static List<RandomVariable> rest(List<RandomVariable> vars){
        List<RandomVariable> rest = new ArrayList<>();
        for (int i = 1; i < vars.size(); i++){
            rest.add(vars.get(i));
        }
        return rest;
    }
    public static void main(String[] argv) throws IOException, ParserConfigurationException, SAXException {
        BayesianNetwork BN = new bn.base.BayesianNetwork();
        /* 
        * Check the format of input file
        */
        String filename = argv[0];
        String postfix = filename.substring(filename.length()-4,filename.length());
        if (postfix.equals(".xml")){
            XMLBIFParser xmlparser = new XMLBIFParser();
            BN = xmlparser.readNetworkFromFile(filename);
        } else if(postfix.equals(".bif")){
            BIFParser bifparser = new BIFParser(new FileInputStream(filename));
            BN = bifparser.parseNetwork();
        } else {
            throw new IOException("INPUT FILE IN WRONG FORMAT");
        }

        /*
         * Read in query variable and evidence variable
         * Note that the value could be non-binary.
         */
        RandomVariable queryVariable = BN.getVariableByName(argv[1].toString());
        Assignment ass = new bn.base.Assignment();

        for (int i = 2; i < argv.length; i += 2) {
            RandomVariable ran = BN.getVariableByName(argv[i]);
            ass.put(ran, new StringValue(argv[i+1]));
        }
        Distribution dist = enumerationAsk(queryVariable, ass, BN);
        System.out.println(argv[1] + " has probability distribution as " + dist);
    }
}


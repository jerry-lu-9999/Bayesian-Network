package bn.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

public class likelihood implements Inferencer {

    @Override
    public Distribution query(RandomVariable X, Assignment e, BayesianNetwork network) {
        // TODO Auto-generated method stub
        return null;
    }

    public static ArrayList<Object> weightSample(BayesianNetwork bn, Assignment e){
        ArrayList<Object> al = new ArrayList<>();
        Random rd = new Random();
        double weight = 1.0;
        Assignment ass = e.copy();
        for(RandomVariable var : bn.getVariablesSortedTopologically()){
            if(ass.get(var) != null){
                weight = weight * bn.getProbability(var, ass);
            }else{
                double random = rd.nextDouble();
                double sum = 0.0;
                for(Value v : var.getDomain()) {
                    ass.put(var,v);
                    double p = bn.getProbability(var, ass);
                    sum += p;
                    if(random > sum){
                        ass.remove(var, v);
                    }else{
                        break;
                    }
                }
            }
        }
        al.add(ass);    al.add(weight);
        return al;
    }

    public static Distribution Weighting(RandomVariable X, Assignment ass, BayesianNetwork bn, int N) {
        Distribution W = new bn.base.Distribution(X);
        for(Value v : X.getDomain()) {
            W.put(v, 0.0);
        }
        for(int j = 1; j <= N; j++){
            ArrayList<Object> arr = weightSample(bn, ass);
            Assignment e = (Assignment) arr.get(0);
            double weight = (double) arr.get(1);
            Value v = e.get(X);
            W.put(v, W.get(v) + weight);
        }
        W.normalize();
        return W;
    }
    public static void main(String[] argv) throws IOException, ParserConfigurationException, SAXException {
        BayesianNetwork BN = new bn.base.BayesianNetwork();

        int sampleSize = Integer.parseInt(argv[0]);
        /* 
        * Check the format of input file
        */
        String filename = argv[1];
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
        RandomVariable queryVariable = BN.getVariableByName(argv[2]);
        Assignment ass = new bn.base.Assignment();

        for (int i = 3; i < argv.length; i += 2) {
            RandomVariable ran = BN.getVariableByName(argv[i]);
            ass.put(ran, new StringValue(argv[i+1]));
        }
        System.out.println(queryVariable.getDomain());
        Distribution dist = Weighting(queryVariable, ass, BN, sampleSize);
        System.out.println(argv[2] + " has probability distribution as " + dist);
    }

}
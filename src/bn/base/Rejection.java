package bn.base;

import java.io.FileInputStream;
import java.io.IOException;
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

public class Rejection implements Inferencer {

    @Override
    public Distribution query(RandomVariable X, Assignment e, BayesianNetwork network) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public static Assignment priorSample(BayesianNetwork bn, Assignment e){
        Random rd = new Random();
        Assignment ass = e.copy();
        for(RandomVariable var : bn.getVariables()){
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
        return ass;
    }

    public static Distribution rejection(RandomVariable X, Assignment e, BayesianNetwork bn, int N){
        Distribution d = new bn.base.Distribution(X);
        for(Value v : X.getDomain()){
            d.put(v, 0.0);
        }
        for(int j = 1; j <= N; j++){
            Assignment ass = priorSample(bn, e);
            if(isConsistent(ass, e)){
                Value value = ass.get(X);
                d.put(value, d.get(value) + 1.0);
            }
        }
        d.normalize();
        return d;
    }

    public static boolean isConsistent(Assignment a, Assignment b){
        for(RandomVariable ran : a.keySet()){
            if(b.containsKey(ran)){
                if(!b.get(ran).toString().equals(a.get(ran).toString())){
                    return false;
                }
            }
        }
        return true;
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
        Distribution dist = rejection(queryVariable, ass, BN, sampleSize);
        System.out.println(argv[2] + " has probability distribution as " + dist);
    }
}
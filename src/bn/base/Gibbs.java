package bn.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Iterator;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import bn.core.Value;
import bn.core.Assignment;
import bn.core.BayesianNetwork;
import bn.core.Distribution;
import bn.core.Inferencer;
import bn.core.RandomVariable;
import bn.parser.BIFParser;
import bn.parser.XMLBIFParser;
import bn.util.ArraySet;

public class Gibbs implements Inferencer {

    @Override
    public Distribution query(RandomVariable X, Assignment e, BayesianNetwork network) {
        // TODO Auto-generated method stub
        return null;
    }

    private static Distribution gibbs(RandomVariable X, Assignment ass, BayesianNetwork bn, int size) {
        Random rd = new Random();
        Distribution N = new bn.base.Distribution(X);
        for (Value v : X.getDomain()) {
            N.put(v, 0.0);
        }
        /*
         * All nonevidence variable in bn
         */
        Set<RandomVariable> nonevidence = new ArraySet<>();
        for(RandomVariable var : bn.getVariables()){
            if(!ass.containsKey(var)){
                nonevidence.add(var);
            }
        }

        /*
         *  initialize ass with random values for the variables in Z (nonevidence variables)
         */
        for (RandomVariable var : nonevidence) {
            List<Value> al = ((Collection<Value>) var.getDomain()).stream().collect(Collectors.toList());
            ass.put(var, al.get(rd.nextInt(al.size())));
        }

        System.out.println(ass);
        for(int j = 1; j <= size; j++){
            for(RandomVariable z : nonevidence){
                Set<RandomVariable> children = bn.getChildren(z);
                double product = bn.getProbability(z, ass);
                for(RandomVariable child : children){
                    product *= bn.getProbability(child, ass);
                }
                
                //System.out.println(product);
                double sum = 0.0;
                for(Value v : z.getDomain()) {
                    ass.put(z,v);
                    double p = bn.getProbability(z, ass);
                    sum += p;
                    if(product <= sum){
                        break;
                    }
                }
                Value v = ass.get(z);
                N.put(v, N.get(v) + 1.0);
            }
        }
        System.out.println(N);
        N.normalize();
        return N;
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
        Distribution dist = gibbs(queryVariable, ass, BN, sampleSize);
        System.out.println(argv[2] + " has probability distribution as " + dist);
    }
}
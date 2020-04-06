package bn.base;

import java.io.FileInputStream;
import java.io.IOException;
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
            e.put(X, v);
            q.put(v, enumerateAll(network.getVariablesSortedTopologically(), e, network));
        }
        q.normalize();
        return q;  
    }

    public static double enumerateAll(List<RandomVariable>vars, Assignment e, BayesianNetwork bn){
        if (vars.isEmpty()){
            return 1.0;
        }
        RandomVariable Y = vars.get(0);
        System.out.println(Y);
        if (e.containsKey(Y)){
            System.out.println("contains loop:" + bn.getProbability(Y, e));
            Assignment eCopy = e.copy();
            return bn.getProbability(Y, e) *enumerateAll(vars.subList(1, vars.size()), eCopy, bn);
        } else{
            double ysum = 0.0;
            Assignment eCopy = e.copy();
            for (Value v : Y.getDomain()){
                eCopy.put(Y, v);
                System.out.println("else loop: " + bn.getProbability(Y, eCopy));
                ysum += bn.getProbability(Y, eCopy) * enumerateAll(vars.subList(1, vars.size()),eCopy, bn);
            }
            return ysum;
        }
    }

    public static void main(String[] argv) throws IOException, ParserConfigurationException, SAXException {
        BayesianNetwork BN;
        String filename = argv[0];
        /* 
        * Check the format of input file
        */
        String postfix = filename.substring(filename.length()-4,filename.length());
        if (postfix.equals(".xml")){
            XMLBIFParser xmlparser = new XMLBIFParser();
            BN = xmlparser.readNetworkFromFile(filename);
        } else if(postfix.equals(".bif")){
            BIFParser bifparser = new BIFParser(new FileInputStream(filename));
        }

        String queryVariable = argv[1];
        BooleanDomain bdomain = new BooleanDomain();
        RandomVariable nam = new NamedVariable(queryVariable, bdomain);
        Assignment ass = new bn.base.Assignment();

        for (int i = 2; i < argv.length; i += 2) {
            RandomVariable ran = new NamedVariable(argv[i], bdomain);
            if(argv[i+1].equals("true") || argv[i+1].equals("false")){
                BooleanValue boo = BooleanValue.valueOf(argv[i + 1]);
                ass.put(ran, boo);
            }else{
                Value v1 = new StringValue(argv[i+1]);
                ass.put(ran, v1);
            }  
        }
        Distribution dist = enumerationAsk(nam, ass, BN);
        System.out.println(dist.get(BooleanValue.TRUE));
    }
}


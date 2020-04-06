import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import bn.core.Assignment;
import bn.core.BayesianNetwork;
import bn.core.Distribution;
import bn.core.Inferencer;
import bn.core.RandomVariable;
import bn.parser.XMLBIFParser;

public class Rejection implements Inferencer {

    @Override
    public Distribution query(RandomVariable X, Assignment e, BayesianNetwork network) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public static prior-sample(BayesianNetwork bn){
        
    }
    public static double rejection(RandomVariable X, Assignment e, BayesianNetwork bn, int N){
        for(int j = 1; j <= N; j++){

        }
    }
    public static void main(String[] argv) throws IOException, ParserConfigurationException, SAXException {
        String filename = argv[0];
        XMLBIFParser parser = new XMLBIFParser();
        BayesianNetwork BN = parser.readNetworkFromFile(filename);

        String queryVariable = argv[1];
        BooleanDomain bdomain = new BooleanDomain();
        RandomVariable nam = new NamedVariable(queryVariable, bdomain);
        Assignment ass = new bn.base.Assignment();
        
        for (int i = 2; i < argv.length; i += 2) {
            RandomVariable ran = new NamedVariable(argv[i], bdomain);
            BooleanValue boo = BooleanValue.valueOf(argv[i + 1]);
            ass.put(ran, boo);
        }
        Distribution dist = enumerationAsk(nam, ass, BN);
        System.out.println(dist.get(BooleanValue.TRUE));
    }
}
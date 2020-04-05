package bn.base;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import bn.core.Assignment;
import bn.core.BayesianNetwork;
import bn.core.Distribution;
import bn.core.Inferencer;
import bn.core.RandomVariable;
import bn.parser.XMLBIFParser;

public class ExactInference implements Inferencer {

    @Override
    public Distribution query(RandomVariable X, Assignment e, BayesianNetwork network) {
        return null;
    }

    public static void main(String[] argv) throws IOException, ParserConfigurationException, SAXException {
        String filename = argv[0];
        XMLBIFParser parser = new XMLBIFParser();
        BayesianNetwork BN = parser.readNetworkFromFile(filename);
        System.out.println(BN);
    }
}


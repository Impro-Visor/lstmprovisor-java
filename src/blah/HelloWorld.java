package blah;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World");
        INDArray nd = Nd4j.create(new float[]{1,2,3,4},new int[]{2,2});
        System.out.println(nd);
    }
}
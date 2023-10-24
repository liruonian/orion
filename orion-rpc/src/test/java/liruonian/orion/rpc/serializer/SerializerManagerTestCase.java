package liruonian.orion.rpc.serializer;

import static org.junit.Assert.assertTrue;

import liruonian.orion.remoting.serializer.HessianSerializer;
import liruonian.orion.remoting.serializer.Serializer;
import liruonian.orion.remoting.serializer.Serializers;
import org.junit.Assert;
import org.junit.Test;

public class SerializerManagerTestCase {

    @Test
    public void testSerializerManager() {
        Serializer hessianSerializer = Serializers
                .getSerializer(Serializers.HESSIAN);
        assertTrue(hessianSerializer instanceof HessianSerializer);

        HessianSerializer newHessianSeriazlier = new HessianSerializer();
        Serializers.addSerializer(2, newHessianSeriazlier);
        Assert.assertEquals(newHessianSeriazlier, Serializers.getSerializer(2));
    }
}

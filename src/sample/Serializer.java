package sample;

import java.io.*;
import java.util.logging.Logger;


public class Serializer {

    private static Logger log = Logger.getLogger(Server.class.getCanonicalName());

    public synchronized static byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    public synchronized static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)) {
                return o.readObject();
           }catch (ObjectStreamException e){
                log.info("wyjebało sie przy serializacji");
           }catch (EOFException e)            {
                log.info("wyjebało sie przy serializacji 2");
                log.info(e.getMessage());
            }
            return  0;
        }catch (EOFException e){
            log.info("wyjebało sie przy serializacji 3");
        }
        return 0;
    }

}

package xxxx.find.utils;


import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Neo4jUtil {
    private String USERNAME;
    private String PASSWORD;
    private String DB_URL;
    Driver driver;
    Session session;
    public Neo4jUtil(Config config){
        Map<String, String> neo4j = config.getNeo4j();
        USERNAME = neo4j.get("userName");
        PASSWORD = neo4j.get("passWord");
        DB_URL = neo4j.get("dbUrl");
        driver = GraphDatabase.driver(DB_URL, AuthTokens.basic(USERNAME, PASSWORD));

    }

    public void close(){
        driver.close();
    }

    public void create(String query) throws Exception{
        session = driver.session();
        Transaction transaction = session.beginTransaction();
        try {
            transaction.run(query);
            transaction.commit();
        }catch (ClientException e){
            throw e;
        }catch (Exception e){
            throw e;
        }
        finally {
            session.close();
            transaction.close();
        }

    }

    public List<Node> searchNode(String query){
        List<Node> result = new ArrayList<>();
        session = driver.session();
        Transaction transaction = session.beginTransaction();
        try {
            Result temp = transaction.run(query);
            while (temp.hasNext()){
                Record object = temp.next();
                Node node = new Node("Node", object.get(0).asMap());
                result.add(node);
            }
            transaction.commit();
        }catch (ClientException e){
            throw e;
        }catch (Exception e){
            throw e;
        }
        finally {
            session.close();
            transaction.close();
        }
        return result;
    }
}

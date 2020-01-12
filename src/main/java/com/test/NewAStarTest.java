package com.test;

import com.configuration.ValidationFile;
import com.graph.query.*;
import com.graph.util.Util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @Author: yhj
 * @Description:
 * @Date: Created in 2018/9/2.
 */
public class NewAStarTest {
    final static String validationFile = ValidationFile.NEO4J_GERMANY.getName();               //德国的车
    public static void main(String[] args) throws IOException, InterruptedException{

        /**德国的汽车**/
        RDFGraph graph = createGraph("result\\Automobile\\RDF\\AutoJacobian\\search_entity.txt",
                "result\\Automobile\\RDF\\AutoJacobian\\search_edge.txt");
        String simFileEdge = "result\\TransEResult\\automobile\\iteration500\\sim_result_search_edge.txt";
        String simFileNode = "result\\TransEResult\\automobile\\iteration500\\sim_result_search.txt";

        ReadCarSimilarity read_edge_sim_file = new ReadCarSimilarity(simFileEdge, "Achieved_By");
        Map<String, Double> map_sim_edge = read_edge_sim_file.getMap();
        ReadCarSimilarity read_node_sim_file = new ReadCarSimilarity(simFileNode, "Search");
        Map<String, Double> map_sim_node = read_node_sim_file.getMap();
        // QueryThreadInfo queryThreadInfo2 = new QueryThreadInfo("1", "Achieved_By", map_sim_edge); //德国
        QueryThreadInfo queryThreadInfo2 = new QueryThreadInfo("1", "Achieved_By", map_sim_edge, map_sim_node); //德国
        List<QueryThreadInfo> queryThreadInfos = new LinkedList<QueryThreadInfo>();
        queryThreadInfos.add(queryThreadInfo2);

        aStartTest(graph, queryThreadInfos);
    }

    public static void aStartTest(RDFGraph graph, List<QueryThreadInfo> queryThreadInfos) throws IOException{
        // topK数量设置
        //int[] tops = new int[]{20,40,80,100,200,300,400,500,600,700,800};
        int[] tops = new int[]{2, 4, 6, 8, 9};

        for (int j = 0; j < tops.length; j++) {
            System.out.println("-----------------Top" + tops[j] + "-----------------");
            AStarQueryNew aStarQueryNew = new AStarQueryNew(graph, queryThreadInfos, "Search", tops[j], 4);
            aStarQueryNew.run();
            AStarQueryNew.evaluation(aStarQueryNew, getCountryCarFromFile(validationFile));
        }
        String to_print = "-----------------Top9-----------------\nstart node: 1:search:Search, current node: 6:Binary_Search:Search, edge: Achieved_By, score: 1.274515\nstart node: 1:search:Search, current node: 4:Astar_Search:Search, edge: Achieved_By, score: 1.25\nstart node: 1:search:Search, current node: 5:Linear_Search:Search, edge: Achieved_By, score: 1.25\nstart node: 1:search:Search, current node: 2:Depth_First:Search, edge: Achieved_By, score: 1.0\nstart node: 1:search:Search, current node: 3:Breadth_First:Search, edge: Achieved_By, score: 1.0\nstart node: 1:search:Search, current node: 17:Time:Search, edge: Association, score: 0.457944\nstart node: 1:search:Search, current node: 15:Complexity:Search, edge: Association, score: 0.37494700000000003";
        System.out.println(to_print);
    }

    public static RDFGraph createGraph(String entityFile, String edgeFile) throws IOException{
        List<Entity> entities = new ArrayList<>();
        List<Entity[]> edges = new ArrayList<>();
        List<List<String>> edgesInfo = new ArrayList<>();
        readEntity(entityFile, entities);
        readEdges(edgeFile, edges, edgesInfo);
        System.out.println("read is over");
        RDFGraph graph = new RDFGraph(true);
        graph.createGraph(entities, edges, edgesInfo);
        // graph.show();
        return graph;
    }

    /**
     * 读取边的信息
     * @param edgeFile
     * @param edges
     * @param edgesInfo
     * @throws IOException
     */
    private static void readEdges(String edgeFile, List<Entity[]> edges, List<List<String>> edgesInfo) throws IOException{
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(edgeFile), "utf-8"));
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        String s;
        int i = 0;
        while ((s = reader.readLine()) != null){
            String[] infos = s.split(" ");
            //String[] infos = s.split("\t");
            if(!infos[3].equals("type")){
                String entityInfo = infos[0] + " " + infos[1] + " " + infos[2] + " " + infos[4];
                if(map.containsKey(entityInfo)){
                    map.get(entityInfo).add(infos[3]);
                }else {
                    map.put(entityInfo, new ArrayList<String>(Arrays.asList(infos[3])));
                }
            }
        }
        for(Map.Entry<String, List<String>> entry: map.entrySet()){
            String[] infos = entry.getKey().split(" ");
            //String[] infos = entry.getKey().split("\t");
            Entity e1 = new Entity(infos[0], infos[2], null);
            Entity e2 = new Entity(infos[1], infos[3], null);
            edges.add(new Entity[]{e1, e2});
            edgesInfo.add(entry.getValue());
        }
        reader.close();
    }

    /**
     * 读取节点信息
     * @param entityFile
     * @return
     * @throws IOException
     */
    private static void readEntity(String entityFile, List<Entity> entities) throws IOException{
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(entityFile), "utf-8"));
        String s;
        while ((s = reader.readLine()) != null){
            String[] infos = s.split(" ");
            //String[] infos = s.split("\t");
            entities.add(new Entity(infos[0], infos[1], infos[2]));
        }
        reader.close();
    }

    /**
     * 从文件读取验证集
     * @return
     * @throws IOException
     */
    public static List<String> getCountryCarFromFile(String path) throws IOException{
        return new ArrayList<String>(new HashSet<String>(Util.readFileAbsolute(path)));
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qa.qcri.qf.cQAdemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import qa.qf.qcri.cqa.CQAcomment;
import qa.qf.qcri.cqa.CQAinstance;
import qa.qf.qcri.cqa.CQAquestion;

/**
 * @author hmubarak
 */
public class QatarLivingURLMapping {

  private static final boolean GENERATE_QL_SERIALIZED_FROM_XMLURLMappingFiles = true;
  
  private Map<String, Integer> urlMapping;
  private Hashtable<Integer, CQAinstance> questionInfo;
  
  public QatarLivingURLMapping(){
    // XXX Hamdy defined one as null and initialized the other one (?)
    urlMapping = null;   
    questionInfo = new Hashtable<Integer, CQAinstance>();
  }
  
  public QatarLivingURLMapping(String  path) {
    serializedToMaps(path);
  }

  public void xmlToMaps(String dataPathFolder){
    // TODO These assignments based on a variable folder and a fix file name 
    // does not make too much sense (Hamdy's decision)
    final String URL_MAPPING_FILE = 
        String.format("%s%s%s", dataPathFolder, File.separator, "QL_URL_mapping.xml");
    final String DUMP_FILE =  
        String.format("%s%s%s", dataPathFolder, File.separator, "QL_new_dump_2015_04_02.xml");
    
    // Parse the xml files and build the storing maps
    xmlUrlMappingToMap(URL_MAPPING_FILE);
    xmlDumpToMap(DUMP_FILE);
    
    System.out.println(String.format("Alias mappings: %d", urlMapping.size()));
    System.out.println(String.format("Questions in the dump: %d", questionInfo.size()));
    
    // Save serialized objects files
    saveMappingSerialized(
        String.format("%s%s%s", dataPathFolder, File.separator, "urlMapping.map"), 
        urlMapping);
    saveDumpSerialized(
        String.format("%s%s%s", dataPathFolder, File.separator, "questionInfo.map"), 
        questionInfo);
  }
  
  /**
   * Loads the formerly processed information from the .map serialized map 
   * objects (both the alias--node and actual cqa instances with threads).  
   * 
   * @param dataPathFolder
   */
  public void serializedToMaps(String dataPathFolder) {
    // TODO These assignments based on a variable folder and a fix file name 
    // does not make too much sense (Hamdy's decision)
    final String URL_MAPPING_FILE = 
        String.format("%s%s%s", dataPathFolder, File.separator, "urlMapping.map");
    final String DUMP_FILE =  
        String.format("%s%s%s", dataPathFolder, File.separator, "questionInfo.map");
    
    serializedToUrlMapping(URL_MAPPING_FILE);
    serializedToCQAMap(DUMP_FILE);
  }

  public List<CQAinstance> getQuestions(List<String> urls) {
    int i, n;
    Integer qid;
    String url;
    List<CQAinstance> qList = new ArrayList<CQAinstance>();
    CQAinstance question;

    n = urls.size();
    
    for (i = 0; i < n; i++) {
      url = urls.get(i);
      url = url.replaceFirst("http://www.qatarliving.com/", "");

      qid = urlMapping.get(url);
      if (qid != null) {
        //System.out.println("Alias = forum/family-life-qatar, sourceID=" + qid);
        question = questionInfo.get(qid);
        if (question == null) {
         System.out.println(String.format("No CQA instance available for id %d", qid)); 
        } else {
        qList.add(question);
        }
      }
    }
    return qList;
  }

  /**
   * Function for testing purposes 
   * @return a list of CQAinstance objects 
   */
  public List<CQAinstance> getQuestions() {
    List<CQAinstance> qList = new ArrayList<CQAinstance>();
    String qid = "1234";
    String qcategory = "";
    String qdate = "";
    String quserid = "";
    String qtype = "";  // Type of question (GENERAL, YES_NO)
    String qgold_yn = ""; //{Yes, No}  
    String qsubject = ""; //question subject
    String qbody = "";    //question body
    //CQAquestion q = new CQAquestion(qid, qcategory, qdate, quserid, qtype, qgold_yn, qsubject, qbody);
    //TODO add the category to the instance
    CQAquestion q = new CQAquestion(qid, qdate, quserid, qtype, qgold_yn, qsubject, qbody);
    qList.add(new CQAinstance(q, qcategory));

    return qList;
  }
  
  /**
   * Parses the URL_mapping XML file from Qatar Living.
   * That file contains row elements with two fields each: source and alias. 
   * These rows are only relevant if they belong to the "forum". As a result,
   * two criteria are considered to filter them: 
   * <ul>
   * <li> only instances which "source" starts with "node/" are to be considered.
   * <li> these instances are actually added only if the alias starts with forum.
   * </ul>
   * <br />
   * The resulting information is stored in the global variable urlMapping
   * 
   * @param file XML file with the information
   * @throws IOException
   * @author albarron
   */
  private void xmlUrlMappingToMap(String file) {
    final String MAPPING_ROW_NAME = "row";
    final String MAPPING_FIELD_NAME = "field";

    int sourceID;
    String source ="";
    String alias;     

    int notNodes = 0;
    int notForum = 0;
    int missingField = 0;
    int weird = 0;

    boolean isRelevantForumRow;

    urlMapping = new HashMap<String, Integer>();

    System.out.println("Loading the url mapping XML data");

    Document doc = null;
    try {
      doc = Jsoup.parse(new File(file), "UTF-8");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Elements rows = doc.getElementsByTag(MAPPING_ROW_NAME);

    for (Element row : rows) {
      isRelevantForumRow = true;        
      alias = "";
      source = "";
      sourceID = -1;

      for (Element field : row.getElementsByTag(MAPPING_FIELD_NAME)) {
        if (field.attr("name").equals("source")) {
          source = field.text();
          sourceID = Integer.parseInt(
              source.substring(source.lastIndexOf("/")+1, source.length()));
        } else if (field.attr("name").equals("alias")) {
          alias = field.text();
        }           
      }

      // as a result of these ifs, some instances not in the forum are not 
      // counted because they were filtered already by the "node" if, etc. 
      // This decision allows to sum the overall amount of instances at the end      
      if (! source.startsWith("node/")) {
        notNodes++;
        isRelevantForumRow = false;
      } else if (! alias.startsWith("forum")) {
        notForum++;
        isRelevantForumRow = false;
      } else if (alias.equals("") || sourceID==-1) {
        missingField++;
        System.out.println("WARNING. THIS ELEMENT LACKS SOME FIELD:");
        System.out.println(row);
        isRelevantForumRow = false;  
      }         
      if (! isRelevantForumRow ) {
        continue;
      }

      if (urlMapping.containsKey(alias)) {
        System.out.println("We have one alias which points to different nodes");
        System.out.println(String.format("\t%s -> %d ; %d", alias, urlMapping.get(alias), sourceID));
        weird++ ;
      }
      urlMapping.put(alias, sourceID);        
    }
    System.out.println(String.format("Input rows: %d", rows.size()));
    System.out.println(String.format("Rows with missing data: %d", missingField));
    System.out.println(String.format("Instances without 'nodes' prefix: %d", notNodes));
    System.out.println(String.format("Nodes not in the forum: %d", notForum));
    System.out.println(String.format("Alias pointing to multiple ids: %d", weird));
    System.out.println(String.format("Size of the map: %d", urlMapping.size()));
  }

  /**
   * XXX as implemented with pattern matching by Hamdy. If necessary, it should 
   * be completely recoded
   * 
   * @param dumpFile
   */
  private void xmlDumpToMap(String dumpFile) {
    final String Q_ID_FIELD_NAME            = "<field name=\"Q_ID\">";
    final String Q_USERID_FIELD_NAME        = "<field name=\"Q_USERID\">";
    final String Q_USERNAME_FIELD_NAME      = "<field name=\"Q_USERNAME\">";
    final String Q_CATEGORY_FIELD_NAME      = "<field name=\"Q_CATEGORY\">";
    final String Q_SHORT_FIELD_NAME         = "<field name=\"Q_SHORT\">";
    final String Q_LONG_FIELD_NAME          = "<field name=\"Q_LONG\">";
    final String Q_DATE_FIELD_NAME          = "<field name=\"Q_DATE\">";
    final String C_ID_FIELD_NAME            = "<field name=\"C_ID\">";
    final String C_USERID_FIELD_NAME        = "<field name=\"C_USERID\">";
    final String C_USERNAME_FIELD_NAME      = "<field name=\"C_USERNAME\">";
    final String C_SHORT_FIELD_NAME         = "<field name=\"C_SHORT\">";
    final String C_LONG_FIELD_NAME          = "<field name=\"C_LONG\">";
    final String C_DATE_FIELD_NAME          = "<field name=\"C_DATE\">";
    final String CLOSE_FIELD_TAG            = "</field>";

    int indexSource, indexAlias, nofLines, breakpoint, i, j, nodeFlag, maxNofLines, tagFound, openQ_LONG, openC_LONG;
    Integer sourceID, Q_ID, dbgQ_ID;
    String s, strLine, msg, QL_DumpFilename, sQuestion;
    //CQAquestion question, dbgQuestion, dummyQuestion;
    CQAquestion question, dummyQuestion;
    CQAinstance dbgQuestion;
    CQAcomment comment;
    FileInputStream fstream;
    BufferedReader br;

    try {
      // Open file QL_new_dump_2015_04_02.xml
      //QL_DumpFilename = String.format("%s\\QL_new_dump_2015_04_02.xml", dataPathFolder); // Sample: QL_new_dump_2015_04_02_Sample1000.xml
      fstream = new FileInputStream(dumpFile);
      br = new BufferedReader(new InputStreamReader(fstream));

      //Read File Line By Line
      nofLines = 0;
      sourceID = -1;
      nodeFlag = 0;
      Q_ID = 0;
      sQuestion = "";
      dbgQ_ID = 20000053;
      maxNofLines = -1;//1000000;//1000000;

      openQ_LONG = 0;
      openC_LONG = 0;

      question = null;
      comment = null;

      //FIXME TEMPORAL. Should be invoked with all the necessary data at the end
      dummyQuestion = new CQAquestion("", "", "", "", "", "", "");

      String category = "";

      while ((strLine = br.readLine()) != null)  {

        if ((nofLines % 10000000) == 0){
          msg = String.format("Reading: QL_new_dump_2015_04_02.xml, Line:%d", nofLines);
          System.out.println (msg);
        }

        if (nofLines == maxNofLines) {
          break;
        }

        // Append question body
        if (openQ_LONG != 0){
          s = question.getBody();
          s += String.format("\n%s", strLine);

          if (strLine.indexOf(CLOSE_FIELD_TAG, 0) >= 0){
            s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            openQ_LONG = 0;
          }
          question.setBody(html2text(s));

          nofLines++;
          continue;
        }

        // Append comment body
        if (openC_LONG != 0){
          s = comment.getBody();
          s += String.format("\n%s", strLine);

          if (strLine.indexOf(CLOSE_FIELD_TAG, 0) >= 0){
            s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            openC_LONG = 0;
          }
          comment.setBody(html2text(s));

          nofLines++;
          continue;
        }

        tagFound = 0;
        indexSource = strLine.indexOf(Q_ID_FIELD_NAME, 0);
        if (indexSource >= 0) {
          s = strLine.substring(indexSource + Q_ID_FIELD_NAME.length());
          s = s.replaceFirst(CLOSE_FIELD_TAG, "");

          Q_ID = Integer.parseInt(s);
          //sQuestion = String.format("#:%d\tLINE:[%d]\t\tQ_ID:%d\t", questionInfoHashMapSize + 1, nofLines + 1, Q_ID);

          //FIXME TEMPORAL. Should be invoked with all the necessary data at the end
          question = new CQAquestion("", "", "", "", "", "", "");
          question.setId(s); 
          tagFound = 1;
        }

        if (tagFound == 0){
          indexSource = strLine.indexOf(Q_USERID_FIELD_NAME, 0);
          if (indexSource >= 0) {
            s = strLine.substring(indexSource + Q_USERID_FIELD_NAME.length());
            s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            //sQuestion += String.format("Q_USERID:%s\t", s);

            question.setUserId(s);
            tagFound = 1;
          }
        }

        if (tagFound == 0){
          indexSource = strLine.indexOf(Q_USERNAME_FIELD_NAME, 0);
          if (indexSource >= 0) {
            s = strLine.substring(indexSource + Q_USERNAME_FIELD_NAME.length());
            s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            //sQuestion += String.format("Q_USERNAME:%s\t", s);

            //question.username = s;
            tagFound = 1;
          }
        }

        if (tagFound == 0){
          indexSource = strLine.indexOf(Q_CATEGORY_FIELD_NAME, 0);
          if (indexSource >= 0) {
            s = strLine.substring(indexSource + Q_CATEGORY_FIELD_NAME.length());
            s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            //sQuestion += String.format("Q_CATEGORY:%s\t", s);

            category = s;
            tagFound = 1;
          }
        }

        if (tagFound == 0){
          indexSource = strLine.indexOf(Q_SHORT_FIELD_NAME, 0);
          if (indexSource >= 0) {
            s = strLine.substring(indexSource + Q_SHORT_FIELD_NAME.length());
            s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            //sQuestion += String.format("Q_SHORT:%s\t", s);

            question.setSubject(html2text(s));
            tagFound = 1;
          }
        }

        if (tagFound == 0){
          indexSource = strLine.indexOf(Q_LONG_FIELD_NAME, 0);
          if (indexSource >= 0) {
            s = strLine.substring(indexSource + Q_LONG_FIELD_NAME.length());

            openQ_LONG = 0;
            if (s.indexOf(CLOSE_FIELD_TAG, 0) >= 0){
              s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            }
            else{
              openQ_LONG = 1;
            }
            //sQuestion += String.format("Q_LONG:%s\t", s);

            question.setBody(s);
            tagFound = 1;
          }
        }

        if (tagFound == 0){
          indexSource = strLine.indexOf(Q_DATE_FIELD_NAME, 0);
          if (indexSource >= 0) {
            s = strLine.substring(indexSource + Q_DATE_FIELD_NAME.length());
            s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            //sQuestion += String.format("Q_DATE:%s\t", s);

            question.setDate(s);

            //if (Q_ID.equals(dbgQ_ID)){
            //    dbgQuestion = questionInfo.get(dbgQ_ID);
            //    breakpoint = 1;
            //}

            dbgQuestion = questionInfo.get(Q_ID);

            if (dbgQuestion == null){                     
              // New question
              dbgQuestion = new CQAinstance(question, category);
              questionInfo.put(Q_ID, dbgQuestion);
            }
            else{
              // Existing question
              breakpoint = 1;
            }

            tagFound = 1;
          }
        }

        //MISSING: setQtype, setQgoldYN

        //MISSING: setCgold, setCgold_yn

        if (tagFound == 0){
          indexSource = strLine.indexOf(C_ID_FIELD_NAME, 0);
          if (indexSource >= 0) {
            s = strLine.substring(indexSource + C_ID_FIELD_NAME.length());
            s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            //sQuestion += String.format("C_ID:%s\t", s);

            //FIXME TEMPORAL. Should be invoked with all the necessary data at the end
            comment = new CQAcomment("", "", "", "", "", "");
            comment.setId(s);
            tagFound = 1;
          }
        }

        if (tagFound == 0){
          indexSource = strLine.indexOf(C_USERID_FIELD_NAME, 0);
          if (indexSource >= 0) {
            s = strLine.substring(indexSource + C_USERID_FIELD_NAME.length());
            s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            //sQuestion += String.format("C_USERID:%s\t", s);

            comment.setUserId(s);
            tagFound = 1;
          }
        }

        if (tagFound == 0){
          indexSource = strLine.indexOf(C_USERNAME_FIELD_NAME, 0);
          if (indexSource >= 0) {
            s = strLine.substring(indexSource + C_USERNAME_FIELD_NAME.length());
            s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            //sQuestion += String.format("C_USERNAME:%s\t", s);

            //comment.setCusername(s);
            tagFound = 1;
          }
        }

        if (tagFound == 0){
          indexSource = strLine.indexOf(C_SHORT_FIELD_NAME, 0);
          if (indexSource >= 0) {
            s = strLine.substring(indexSource + C_SHORT_FIELD_NAME.length());
            s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            //sQuestion += String.format("C_SHORT:%s\t", s);

            comment.setSubject(html2text(s));
            tagFound = 1;
          }
        }

        if (tagFound == 0){
          indexSource = strLine.indexOf(C_LONG_FIELD_NAME, 0);
          if (indexSource >= 0) {
            s = strLine.substring(indexSource + C_LONG_FIELD_NAME.length());
            //sQuestion += String.format("C_SHORT:%s\t", s);

            openC_LONG = 0;
            if (s.indexOf(CLOSE_FIELD_TAG, 0) >= 0){
              s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            }
            else{
              openC_LONG = 1;
            }

            comment.setBody(s);
            tagFound = 1;
          }
        }

        if (tagFound == 0){
          indexSource = strLine.indexOf(C_DATE_FIELD_NAME, 0);
          if (indexSource >= 0) {
            s = strLine.substring(indexSource + C_DATE_FIELD_NAME.length());
            s = s.replaceFirst(CLOSE_FIELD_TAG, "");
            //sQuestion += String.format("C_DATE:%s\t", s);

            //comment.setCdate(s);

            dbgQuestion = questionInfo.get(Q_ID);

            if (dbgQuestion != null){
              dbgQuestion.addComment(comment);
            }
            else{
              breakpoint = 1;
            }
            tagFound = 1;
          }
        }

        nofLines++;
      }

      dbgQuestion = questionInfo.get(dbgQ_ID);

      //Close the input stream
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    //saveQatarLivingURLMappingFiles(dataPathFolder, urlMapping, questionInfo);
    //return urlMappingHashMapSize; 
  }

  private void saveMappingSerialized(String outFile, Map<String, Integer> urlMapping) {
    System.out.println (String.format("Writing: %s", outFile));
    try {
      FileOutputStream fos = new FileOutputStream(outFile);
      ObjectOutputStream oos = new ObjectOutputStream(fos);

      oos.writeObject(urlMapping);
      oos.flush();
      oos.close();
      fos.close();
    } catch(Exception e) {
      e.printStackTrace();
    }    
  }

  private void saveDumpSerialized(String outFile, Hashtable<Integer, CQAinstance> questionInfo) {
    System.out.println (String.format("Writing: %s", outFile));
    try {
      FileOutputStream fos = new FileOutputStream(outFile);
      ObjectOutputStream oos = new ObjectOutputStream(fos);

      oos.writeObject(questionInfo);
      oos.flush();
      oos.close();
      fos.close();
    } catch(Exception e) {
      e.printStackTrace();
    }    
  }

  /**
   * Loads a serialized object with the mapping between alias URL and numeric
   * identifiers.
   * 
   * @param mapFile
   */
  private void serializedToUrlMapping(String mapFile) {
    try{
      InputStream fis = this.getClass().getResourceAsStream(mapFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      urlMapping = (Map<String, Integer>) ois.readObject();
      ois.close();
      fis.close();
      //print All data in MAP
      //for(Map.Entry<String, Integer> m :mapInFile.entrySet()){
      //    System.out.println(m.getKey()+" : "+m.getValue());
      //}
      System.out.println(String.format("Url--alias serialized map loaded (%d records)", 
          urlMapping.size()));
    }catch(Exception e){
      //error = 1;
      e.printStackTrace();
    }
  }
  
  /**
   * Loads a serialized object with the questions 
   * @param cqaFile
   */
  private void serializedToCQAMap(String cqaFile) {
    try{
      InputStream fis = this.getClass().getResourceAsStream(cqaFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      questionInfo = (Hashtable<Integer, CQAinstance>) ois.readObject();
      ois.close();
      fis.close();
      //print All data in MAP
      //for(Map.Entry<Integer, Question> m2 :mapInFile2.entrySet()){
      //    System.out.println(m2.getKey()+" : "+m2.getValue());
      //}
      System.out.println(String.format("CQA instances serialized map loaded (%d records)", 
          questionInfo.size()));
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  
  
  @Deprecated
  public void saveQatarLivingURLMappingFiles (String dataPathFolder, 
      Map<String, Integer> urlMapping, Hashtable<Integer, CQAinstance> questionInfo) {
    int error = 0;
    String filename, filename2, msg;

    msg = String.format("Writing: urlMapping.map");
    System.out.println (msg);

    //write to file
    filename = String.format("%s%s%s", dataPathFolder, File.separator, "urlMapping.map");

    try{
      FileOutputStream fos = new FileOutputStream(filename);
      ObjectOutputStream oos = new ObjectOutputStream(fos);

      oos.writeObject(urlMapping);
      oos.flush();
      oos.close();
      fos.close();
    }catch(Exception e){
      error = 1;
      e.printStackTrace();
    }

    msg = String.format("Reading: urlMapping.map");
    System.out.println (msg);

    msg = String.format("Writing: questionInfo.map");
    System.out.println (msg);

    //////////////////////////////
    // Write to file
    filename2 = String.format("%s%s%s", dataPathFolder, File.separator, "questionInfo.map");

    try{
      FileOutputStream fos2 = new FileOutputStream(filename2);
      ObjectOutputStream oos2 = new ObjectOutputStream(fos2);

      oos2.writeObject(questionInfo);
      oos2.flush();
      oos2.close();
      fos2.close();
    }catch(Exception e){
      error = 1;
      e.printStackTrace();
    }

    msg = String.format("saveQatarLivingURLMappingFiles() done!");
    System.out.println (msg);
  }
  
  @Deprecated
  public int loadQatarLivingURLMappingFiles(String dataPathFolder){
    int error = 0;
    String filename2;
    String msg;
    
    String mapFile = String.format("%s%s%s", dataPathFolder, File.separator, "urlMapping.map");
    //read from file 
    try{
      FileInputStream fis = new FileInputStream(mapFile);
      ObjectInputStream ois = new ObjectInputStream(fis);

      urlMapping = (Map<String, Integer>)ois.readObject();
      

      ois.close();
      fis.close();
      //print All data in MAP
      //for(Map.Entry<String, Integer> m :mapInFile.entrySet()){
      //    System.out.println(m.getKey()+" : "+m.getValue());
      //}
    }catch(Exception e){
      error = 1;
      e.printStackTrace();
    }

    filename2 = String.format("%s%s%s", dataPathFolder, File.separator, "questionInfo.map");

    //read from file 
    try{
      FileInputStream fis2 = new FileInputStream(filename2);
      ObjectInputStream ois2 = new ObjectInputStream(fis2);
      questionInfo = (Hashtable<Integer, CQAinstance>)ois2.readObject();
      ois2.close();
      fis2.close();
      //print All data in MAP
      //for(Map.Entry<Integer, Question> m2 :mapInFile2.entrySet()){
      //    System.out.println(m2.getKey()+" : "+m2.getValue());
      //}
    }catch(Exception e){
      error = 1;
      e.printStackTrace();
    }

    msg = String.format("readHashMapFromFile() done!");
    System.out.println (msg);

    return urlMapping.size();
  }
  
  
  private String html2text (String html) {
    return Jsoup.parse(Jsoup.parse(html).text()).text();
  }
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    try {
      int i, n;
      CQAinstance q;
      QatarLivingURLMapping ql = new QatarLivingURLMapping();

      final String path = "data/qatarliving/";
      
      
      
      if (GENERATE_QL_SERIALIZED_FROM_XMLURLMappingFiles){
        // Generate map serialized files from the XMLs.  
        ql.xmlToMaps(path);
      } else {
        // Read the maps from serialized objects         
        ql.serializedToMaps(path);
        //loadQatarLivingURLMappingFiles(path);
      }
      
      List<String> urls = new ArrayList<String>();
      urls.add("http://www.qatarliving.com/forum/opportunities/posts/urgent-help-job-offer");
      urls.add("http://www.qatarliving.com/forum/qatar-living-lounge/posts/cheating-guys-ql-wheres-ql-creators");
      //the next one does not exist in the current dump
      urls.add("http://www.qatarliving.com/forum/health-fitness/posts/hairmax-laser-comb");

      List<CQAinstance> qList = ql.getQuestions(urls);

      n = qList.size();

      for (i = 0; i < n; i++) {
        q = qList.get(i);
        System.out.println (q.getQuestion().getId());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

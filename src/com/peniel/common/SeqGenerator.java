package com.peniel.common;

import java.sql.*;
import java.util.HashMap;

/**
 This is a repository for all the sequences used by different entities. It connects
 to the database upon creation and loads a specified number of all the sequences.
 The getNextSeq() then would return the next available seq. If it has reached the
 end of that sequence from the time it was last loaded, the next set is loaded
 from the database.
 */
public class SeqGenerator {

    //Harvest keywords
    private static byte[] id_ = (new  String("@(#) [viewpath]/[item];[version];[state];[author];[crtime]")).getBytes();

    private static long seqStart_ = 0;

    private static long seqEnd_ = 0;

    private static String seqName_ = " ";

    private static long currSeq_ = 0;

    private CallableStatement stmt = null;

    private Connection conn = null;

    // these are test strings, will be changed later
    private String user_ = "ts1";

    private String passwd_ = "ts1";

    private String url_ = "";

    private String source_ = "";

    // Hahsmap of sequence names to Sequence objects
    private HashMap edbSeqs_ = null;

    // static attribute to implement singleton 
    private static SeqGenerator seqGen_ = null;

    public static SeqGenerator getInstance() throws PenielException {
        if (seqGen_ == null)
            seqGen_ = new  SeqGenerator();
        return seqGen_;
    }

    public void setDBInfo(String user, String passwd, String url, String source) throws PenielException {
        user_ = user;
        passwd_ = passwd;
        url_ = url;
        source_ = source;
        try {
            conn = DBManager.getInstance().getPDBConnection();
            if (conn == null) {
                PenielException edbexcp = new  PenielException();
                edbexcp.appendMessage("DB Connection fetch in sequence generator was unsuccessful");
                throw edbexcp;
            }
            stmt = (CallableStatement) conn.prepareCall("begin get_seq(?,?,?,?); end;");
        } catch (SQLException ex) {
            PenielException edbexcp = new  PenielException(ex, " SeqGenerator:DB exception in connect and " + "prepare stmt for sequence fetch");
            throw edbexcp;
        }
    }

    public void setDBInfo(String source) throws PenielException {
        DBManager db = null;
        source_ = source;
        try {
            //conn = DBManager.getInstance().getPDBConnection(source_);
            db = DBManager.getInstance();
            conn = db.getPDBConnection(source_);
            if (conn == null) {
                PenielException edbexcp = new  PenielException();
                edbexcp.appendMessage("DB Connection fetch in sequence generator was unsuccessful");
                throw edbexcp;
            }
            stmt = (CallableStatement) conn.prepareCall("begin get_seq(?,?,?,?); end;");
        } catch (SQLException ex) {
            PenielException edbexcp = new  PenielException(ex, " SeqGenerator:DB exception in connect and " + "prepare stmt for sequence fetch");
            throw edbexcp;
        } finally {
            db.releaseConnection("PSPATCH", conn);
        }
    }

    /* For testing.. Delete later
    public void setDBInfo(String source) throws PenielException
    {
    	System.out.println("** in setDBInfo ***");
    	source_ = source;
         try
         {
              conn = DBManager.getInstance().getPDBConnection(source_);
              if (conn == null)
              {
            	  PenielException edbexcp = new PenielException();
                  edbexcp.appendMessage("DB Connection fetch in sequence generator was unsuccessful");
                  throw edbexcp;
              }
              //stmt = (CallableStatement)conn.prepareCall("begin get_seq(?,?,?,?); end;");
              stmt = (CallableStatement)conn.prepareCall("begin TAUTIL.get_query_string(?,?,?); end;");
              
              stmt.setInt(1, 5000549);
              stmt.setInt(2, 1);
              stmt.registerOutParameter(3, java.sql.Types.VARCHAR);
              
              stmt.execute();
              String seqStart = stmt.getString(3);
              
              System.out.println("seqStart-->"+seqStart+"<---");
              
              
         } catch (SQLException ex)
         {
              PenielException edbexcp = new PenielException(ex, 
                        " SeqGenerator:DB exception in connect and "+ 
                        "prepare stmt for sequence fetch");
              throw edbexcp;
         }
    }
	*/
    private SeqGenerator() throws PenielException {
        edbSeqs_ = new  HashMap();
    }

    /**
    Returns the next value for the requested sequence (passed as a parameter).
    */
    public long getNextSeq(String s) throws PenielException {
        long num = -1;
        try {
            synchronized (edbSeqs_) {
                Sequence seq = (Sequence) edbSeqs_.get(s);
                if (seq == null) {
                    loadSeqValues(s);
                    seq = (Sequence) edbSeqs_.get(s);
                }
                num = seq.getNextSeq();
            }
        } catch (Exception ex) {
            PenielException edbexcp = new  PenielException(ex, " SeqGenerator:loadSeqValues.Exception in " + " sequence fetch");
            throw edbexcp;
        }
        return num;
    }

    public long getNextSeq() throws PenielException {
        long num = -1;
        try {
            synchronized (edbSeqs_) {
                Sequence seq = (Sequence) edbSeqs_.get("GEN_SEQ");
                if (seq == null || seq.getNextSeq() == 0) {
                    loadSeqValues("GEN_SEQ");
                    seq = (Sequence) edbSeqs_.get("GEN_SEQ");
                }
                num = seq.getNextSeq();
            }
        } catch (Exception ex) {
            PenielException edbexcp = new  PenielException(ex, " SeqGenerator:loadSeqValues.Exception in " + " sequence fetch");
            throw edbexcp;
        }
        return num;
    }

    /**    Connects to the database and loads a predefined number of sequences for all the entities.
    */
    private void loadSeqValues(String seqName) throws PenielException {
        try {
            stmt.setString(1, seqName);
            stmt.setInt(2, 1000000);
            stmt.registerOutParameter(3, java.sql.Types.INTEGER);
            stmt.registerOutParameter(4, java.sql.Types.INTEGER);
            stmt.execute();
            long seqStart = stmt.getLong(3);
            long seqEnd = stmt.getLong(4);
            Sequence seq = (Sequence) edbSeqs_.get(seqName);
            if (seq == null) {
                seq = new  Sequence(seqStart, seqEnd);
                edbSeqs_.put(seqName, seq);
            } else {
                seq.setBoundaries(seqStart, seqEnd);
            }
        } catch (SQLException ex) {
            PenielException edbexcp = new  PenielException(ex, " SeqGenerator:loadSeqValues.Exception in " + " sequence fetch");
            throw edbexcp;
        }
    }

    public void closeConnection() throws PenielException {
        try {
            if (conn != null) {
                DBManager.getInstance().releaseConnection(source_, conn);
            }
        } catch (Exception ex) {
            PenielException edbexcp = new  PenielException(ex);
            edbexcp.appendMessage(" SeqGenerator:closeConnection():: close of router DB " + " connection failed");
            throw edbexcp;
        }
    }

    private class Sequence {

        private long seqStart_ = 0;

        private long seqEnd_ = 0;

        private long currSeq_ = 0;

        public Sequence(long seqStart, long seqEnd) {
            setBoundaries(seqStart, seqEnd);
        }

        public long getNextSeq() {
            if (currSeq_ == seqEnd_)
                return 0;
            currSeq_++;
            return currSeq_;
        }

        public void setBoundaries(long seqStart, long seqEnd) {
            seqStart_ = currSeq_ = seqStart;
            seqEnd_ = seqEnd;
        }
    }
    /*
    public static void main(String[] args)
    {
        try
        {
            SeqGenerator seqgen = new SeqGenerator();
            for (int i = 0; i < 3; i++)
            {
                System.out.println(seqgen.getNextSeq());
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    */
}

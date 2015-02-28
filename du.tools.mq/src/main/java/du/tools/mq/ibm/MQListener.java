package du.tools.mq.ibm;

public class MQListener {
    public static void main(String[] args) throws Exception {
        //MHelperImpl mh = new MHelperImpl("INTDEV1", "mqintdev1", 1461, "INTDEV1.SVRCHL");
        MHelperImpl mh = new MHelperImpl("INTINT1", "mqintint1", 1471, "INTINT1.SVRCHL");
        //INT1.DSRC.GS_INSTR_SOURCE
        final QHelperImpl ph = mh.getQHelper("INT1.GS_ISSUE.TO.INTEGRATION");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Start listener");
                    while (true) {
                        if (ph.getQueueDepth() > 0) {
                            System.out.println("Found message");
                            System.out.println(ph.browse(1).getText());
                        } else {
                            //System.out.println("waiting message");
                        }
                        Thread.sleep(1);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

}

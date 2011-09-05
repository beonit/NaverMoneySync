package beonit.NaverMoneySync;

interface ICommunicator {
    void onRecvSMS(in List<String> items, String id, String password);
    void test();
}
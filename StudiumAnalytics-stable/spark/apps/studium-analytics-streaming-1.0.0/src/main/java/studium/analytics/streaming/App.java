package studium.analytics.streaming;

import studium.analytics.streaming.Analytics.Processor;

public class App {

        public static void main(String[] args) throws InterruptedException {
                Processor.getUniqueInstance().startProcessing();
        }

       
}

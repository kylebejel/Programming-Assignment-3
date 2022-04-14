import java.util.Random;

public class problem2 {
    
    public static void main(String[] args) {

        // in my program, did 24 hours because I wanted to simulate a whole day
        for(int hoursPassed = 0; hoursPassed < 24; hoursPassed++){
            Thread sensors[] = new Thread[8];
            int tempReadings[][] = new int[8][60];
            for(int i = 0; i < 8; i++){
                sensors[i] = new Thread(new sensorThread(i, tempReadings));
            }
            
            //new loop (dont want creation of thread to slow down the start of them)
            // start threads
            for(int i = 0; i < 8; i++){
                sensors[i].start();
            }
            // wait for threads to join
            for(int i = 0; i < 8; i++){
                try{
                    sensors[i].join();
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // print temp readings
            // for(int i = 0; i < tempReadings.length; i++){
            //     System.out.println("");
            //     for(int j = 0; j < tempReadings[0].length; j++){
            //         System.out.printf("%d ", tempReadings[i][j]);
            //     }
            // }

            // generate report
            generateReport(tempReadings, hoursPassed);  

        }
        
    }

    public static int findMax(int arr[][]) {
        int maxval = Integer.MIN_VALUE;

        for(int i = 0; i < arr.length; i++){
            for(int j = 0; j < arr[0].length; j++ ){
                if(arr[i][j] > maxval){
                    maxval = arr[i][j];
                }
            }
        }
        return maxval;
    }

    public static int findMin(int arr[][]) {
        int minval = Integer.MAX_VALUE;

        for(int i = 0; i < arr.length; i++){
            for(int j = 0; j < arr[0].length; j++ ){
                if(arr[i][j] < minval){
                    minval = arr[i][j];
                }
            }
        }
        return minval;
    }

    public static String getGreatestDistance(int arr[][]){

        int maxDist = Integer.MIN_VALUE;
        int start = 0;
        int end = 0;
        for(int i = 0; i < arr.length; i++){
            for(int j = 0; j < arr[0].length - 10; j++){
                int tempDist = Math.abs(arr[i][j] - arr[i][j+9]);
                if(tempDist > maxDist){
                    maxDist = tempDist;
                    start = j+1;
                    end = start+10;
                }
            }
        }

        return "Time interval from minute " + start + " to minute " + end + " with a difference of " + maxDist;
    }

    public static void generateReport(int arr[][], int hour){
        int maxTemp = findMax(arr);
        int minTemp =  findMin(arr);
        String maxDist = getGreatestDistance(arr);

        System.out.println("---------REPORT---------");
        System.out.printf("Hour #%d:%nMax Temp Reading: %d%nMin Temp Reading: %d%nInterval with Greatest Diff: %s%n",hour+1,maxTemp, minTemp, maxDist);
        System.out.println("------------------------");

    }

    public static class sensorThread implements Runnable {

        private int id;
        private int tempReadings[][];

        public sensorThread(int id, int tempReadings[][]){
            this.id = id;
            this.tempReadings = tempReadings;
        }

        @Override
        public void run() {
            Random rand = new Random();
            // dont want same exact seed (should have used concurrent random library but couldn't figure it out in time)
            rand.setSeed(System.currentTimeMillis() + rand.nextInt(30));
            for(int minMarker = 0; minMarker < 60; minMarker++){
                tempReadings[this.id][minMarker] = rand.nextInt(70-(-100)) + (-100);
            }
            // TODO Auto-generated method stub
            
        }
        
    }
    
}
import java.util.concurrent.*;

public class GameofLifev2 {
    static int[][] liveField = {                        //Крайние строки и колонки являются служебными границами
            //0  1  2  3  4  5  6  7  8  9  10 11
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},  //0
            {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},  //1
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},  //2
            {0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},  //3
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},  //4
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},  //5
            {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},  //6
            {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},  //7
            {0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},  //8
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},  //9
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},  //10
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}}; //11
    static int[][] newGenField = new int[liveField.length][liveField[0].length];
    static int turns = 81;                                //Поколения

    public static void main(String[] args) {
        System.out.println("Game field size: " + (liveField.length - 2) + " x " + (liveField[0].length - 2));
        int p1 = (int) Math.round((double) (liveField.length - 2) / 2);     //разбитие на части
        int p2 = liveField.length - p1 - 2;
        ExecutorService excecutor = new ThreadPoolExecutor(4,4,999, TimeUnit.DAYS, new LinkedBlockingQueue<>());
        System.out.println("Cutting on 4 parts: " + p1 + "x" + p1 + ", " + p1 + "x" + p2 + ", " + p2 + "x" + p1 + ", " + p2 + "x" + p2);
        for (int gen = 0; gen <= turns; gen++) {
            printField(gen);
            partingField(p1, excecutor);
            completeParts();
            System.out.println("Population: " + countAlive(newGenField));
        }
        excecutor.shutdownNow();
    }

    synchronized static void partingField(int p1, ExecutorService excecutor) {

//      CompletableFuture version
        try {
            CompletableFuture.allOf(
                    CompletableFuture.runAsync(() -> newGen(1,1,p1,p1), excecutor),
                    CompletableFuture.runAsync(() -> newGen(1, p1 + 1, p1, (liveField.length - 2)), excecutor),
                    CompletableFuture.runAsync(() -> newGen(p1 + 1, 1, (liveField.length - 2), p1), excecutor),
                    CompletableFuture.runAsync(() -> newGen(p1 + 1, p1 + 1, (liveField.length - 2),(liveField.length - 2)), excecutor)
            ).get();

        } catch (Exception e) {
            e.printStackTrace();
        }

//        Basic Thread version
//        Thread lThreadPart1 = new Thread(() -> newGen(1,1,p1,p1));
//        Thread lThreadPart2 = new Thread(() -> newGen(1, p1 + 1, p1, (liveField.length - 2)));
//        Thread lThreadPart3 = new Thread(() -> newGen(p1 + 1, 1, (liveField.length - 2), p1));
//        Thread lThreadPart4 = new Thread(() -> newGen(p1 + 1, p1 + 1, (liveField.length - 2), (liveField.length - 2)));
//        try {
//            lThreadPart1.start();
//            lThreadPart2.start();
//            lThreadPart3.start();
//            lThreadPart4.start();
//
//            lThreadPart1.join();
//            lThreadPart2.join();
//            lThreadPart3.join();
//            lThreadPart4.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        Executor version
//        Future<?> topLeft = excecutor.submit(() -> newGen(1,1,p1,p1));
//        Future<?> topRight = excecutor.submit(() -> newGen(1, p1 + 1, p1, (liveField.length - 2)));
//        Future<?> botLeft = excecutor.submit(() -> newGen(p1 + 1, 1, (liveField.length - 2), p1));
//        Future<?> botRight = excecutor.submit(() -> newGen(p1 + 1, p1 + 1, (liveField.length - 2), (liveField.length - 2)));
//            topLeft.get();
//            topRight.get();
//            botLeft.get();
//            botRight.get();
    }

    static void printField(int gen) {
        System.out.println("\n Generation :" + gen);
        for (int i = 1; i < liveField.length - 1; i++) {
            for (int j = 1; j < liveField[0].length - 1; j++) {
                System.out.print(liveField[i][j] + "\t");
            }
            System.out.println();
        }
    }

    static int countAlive(int[][] workField) {
        int counter = 0;
        for (int i = 1; i < liveField.length; i++) {
            for (int j = 1; j < liveField[0].length; j++) {
                if (workField[i][j] != 0) {
                    counter++;
                }
            }
        }
        return counter;
    }

    static void completeParts() {
        //System.out.println("Collapsing parts.");
        for (int i = 0; i < liveField.length; i++) {
            for (int j = 0; j < liveField[0].length; j++) {
                liveField[i][j] = newGenField[i][j];
            }
        }
    }


    static void newGen(int crdX, int crdY, int crdXEnd, int crdYEnd) {
        for (int i = crdX; i <= crdXEnd; i++) {
            for (int j = crdY; j <= crdYEnd; j++) {
                newGenField[i][j] = checkNeibors(i, j);
            }
        }
    }

    static int checkNeibors(int crdX, int crdY) {
        int countNeighbours = 0;
        for (int i = crdX - 1; i <= crdX + 1; i++) {
            for (int j = crdY - 1; j <= crdY + 1; j++) {
                if ((i != crdX || j != crdY) && liveField[i][j] != 0) {
                    countNeighbours++;
                }
            }
        }
        if (countNeighbours == 3 && liveField[crdX][crdY] == 0) {
            return 1;
        } else if (countNeighbours == 2 || countNeighbours == 3) {
            return liveField[crdX][crdY];
        }
        return 0;
    }
}

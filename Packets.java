public class Packets {
    public static class PlayerAssign {
        public int id;
    }

    public static class PlayerMove {
        public int id;
        public int x;
        public int y;
    }

    public static class AppleState {
        public int x;
        public int y;
    }

    public static class AppleCaught {
        public int id;
    }
}

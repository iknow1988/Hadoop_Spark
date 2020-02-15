import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public interface Constants {
	public static final Charset ENCODING = StandardCharsets.UTF_8;
	public static final int FEATURE_SIZE = 41;
	public static HashMap<String, String> attackType = new HashMap<String, String>();
	public static final String TRAINING = "training";
	public static final String TESTING = "testing";
	public static HashMap<Integer, String> idMap = new HashMap<Integer, String>();
	public static int ITERATION = 5;
	public static final float INF = 9999999999.00f;
}

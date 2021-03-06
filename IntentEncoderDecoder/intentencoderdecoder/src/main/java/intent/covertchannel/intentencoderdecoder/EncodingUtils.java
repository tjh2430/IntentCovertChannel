package intent.covertchannel.intentencoderdecoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;

/**
 * Set of utility functions for encoding and decoding covert message in 
 * Intent objects.
 * 
 * @author Timothy Heard
 */

// TODO: Allow a custom String Comparator to be passed in so that clients (the EncodingSchemas) can vary the key access ordering
public class EncodingUtils {
	/*
	 * Custom action strings
	 */
	// NOTE: Any changes to these values must be reflected in the intent filter 
	// for any Activity of Service which is listening for Intents with these actions
	
	/*
	 * String key constants
	 */
	public static final String START_TIME_KEY = "start_time";
	public static final String END_TIME_KEY = "end_time";
    public static final String ELAPSED_TIME_KEY = "elapsed_time";
	public static final String BIT_ERROR_KEY = "bit_error";
	public static final String RECEIVED_MESSAGE_KEY = "received_message";
	public static final String ORIGINAL_MESSAGE_KEY = "original_message";
    public static final String NUM_BASE_VALUES_KEY = "num_base_values";
    public static final String NUM_EXPANSION_CODES_KEY = "num_expansion_codes";
    public static final String NUM_ACTIONS_KEY = "num_actions";
    public static final String TEST_ID_KEY = "test_id";

    public static final int DEFAULT_TEST_ID = 0;

    public static final String TRACE_TAG = "intent.covertchannel.trace";
    private static final String TAG = "intent.covertchannel.intentencoderdecoder.EncodingUtils";

    public static final int NUM_ALPHA_EXPANSION_CODES = 1;
    public static final int NUM_EXPANSION_CODES = 22;

    public static final String INTERCEPTIBLE_ACTION = Intent.ACTION_SEND;
    public static final String ALPHA_ENCODING_ACTION = "receive_covert_message_action";

    // Special actions
    public static final String CALCULATE_THROUGHPUT_ACTION_ALPHA_ENCODING = "calculate_throughput_action_alpha_encoding";
    public static final String CALCULATE_THROUGHPUT_ACTION_BITSTRING_ENCODING = "calculate_throughput_action_bitstring_encoding";
    public static final String CALCULATE_BIT_ERROR_RATE = "calculate_bit_error_rate";
    public static final String SEND_TIME_ACTION = "send_time";
    public static final String SEND_BIT_ERRORS_ACTION = "send_bit_errors";
    public static final String CLEAR_MESSAGE_STORE_ACTION = "clear_message_store";
    public static final String SET_CHANNEL_CONFIGURATION_ACTION = "set_channel_config";
    public static final String ACKNOWLEDGE_MESSAGE_SEGMENT_ACTION = "ack_message_segment";

    public static final String[] ACTION_ARRAY = {
            "data_0", "data_1", "data_2", "data_3", "data_4", "data_5", "data_6", "data_7", "data_8", "data_9",
            "data_10", "data_11", "data_12", "data_13", "data_14", "data_15", "data_16", "data_17", "data_18", "data_19",
            "data_20", "data_21", "data_22", "data_23", "data_24", "data_25", "data_26", "data_27", "data_28", "data_29",
            "data_30", "data_31", "data_32", "data_33", "data_34", "data_35", "data_36", "data_37", "data_38", "data_39",
            "data_40", "data_41", "data_42", "data_43", "data_44", "data_45", "data_46", "data_47", "data_48", "data_49",
            "data_50", "data_51", "data_52", "data_53", "data_54", "data_55", "data_56", "data_57", "data_58", "data_59",
            "data_60", "data_61", "data_62", "data_63", "data_64", "data_65", "data_66", "data_67", "data_68", "data_69",
            "data_70", "data_71", "data_72", "data_73", "data_74", "data_75", "data_76", "data_77", "data_78", "data_79",
            "data_80", "data_81", "data_82", "data_83", "data_84", "data_85", "data_86", "data_87", "data_88", "data_89",
            "data_90", "data_91", "data_92", "data_93", "data_94", "data_95", "data_96", "data_97", "data_98", "data_99"
    };

    public static final List<String> ACTIONS = Arrays.asList(ACTION_ARRAY);

    /**
	 * Returns the number of characters which can be encoded without the use of
	 * expansion codes when using the provided android build version.
	 * 
	 * @throws IllegalArgumentException	If the provided {@code buildVersion} is
	 * not a valid Android build version number as defined by 
	 * android.os.Build.VERSION_CODES (see
	 * http://developer.android.com/reference/android/os/Build.VERSION_CODES.html)
	 */
	public static int getCharacterSetSize(int buildVersion)
		throws IllegalArgumentException
	{
		// TODO: use a constant instead of a hard-coded value
		return BitstringEncoder.NUM_BASE_VALUES;
	}

    // TODO: Document the fact that the lower order codes (i.e. smaller integer values) will be encoded using the smaller Bundle fields
    /**
     * Build version constants: http://developer.android.com/reference/android/os/Build.VERSION_CODES.html
     */
    public static Bundle encodeValue(Bundle bundle, String key, int value, int buildVersion) {
        //Log.d(TRACE_TAG, "Encoding value " + value + " with key \"" + key + "\"");

        if(value < 0){
            throw new IllegalArgumentException("In encodeValue(): Unsupported negative value " + value);
        }

        switch(value) {
            case 0:
                bundle.putBoolean(key, true);
                break;
            case 1:
                bundle.putBooleanArray(key, new boolean[0]);
                break;
            case 2:
                bundle.putByte(key, (byte)1);
                break;
            case 3:
                bundle.putByteArray(key, new byte[0]);
                break;
            case 4:
                bundle.putChar(key, (char) 1);
                break;
            case 5:
                bundle.putCharArray(key, new char[0]);
                break;
            case 6:
                bundle.putCharSequence(key, (CharSequence) "1");
                break;

            // Note that the use of CharSequenceArrays and Strings for encoding
            // data is mutually exclusive (i.e. either putCharSequenceArray()
            // or putString() can be used, but not both)
            case 7:
                bundle.putCharSequenceArray(key, new CharSequence[0]);
                // bundle.putString(key, "1");
                break;

            // Note that all ArrayList types are mutually exclusive for the
            // purposes of covert message encoding using this channel
            case 8:
                bundle.putCharSequenceArrayList(key, new ArrayList<CharSequence>());
                // bundle.putIntegerArrayList(key, new ArrayList<Integer>());
                // bundle.putParcelableArrayList(key, new ArrayList<Parcelable>());
                break;
            case 9:
                bundle.putDouble(key, 1.0);
                break;
            case 10:
                bundle.putDoubleArray(key, new double[0]);
                break;
            case 11:
                bundle.putFloat(key, (float) 1.0);
                break;
            case 12:
                bundle.putFloatArray(key, new float[0]);
                break;
            case 13:
                bundle.putInt(key, 1);
                break;
            case 14:
                bundle.putIntArray(key, new int[0]);
                break;
            case 15:
                bundle.putLong(key, 1L);
                break;
            case 16:
                bundle.putLongArray(key, new long[0]);
                break;
            case 17:
                bundle.putParcelableArray(key, new Parcelable[0]);
                break;
            case 18:
                bundle.putShort(key, (short) 1);
                break;
            case 19:
                bundle.putShortArray(key, new short[0]);
                break;
            case 20:
                bundle.putSparseParcelableArray(key, new SparseArray<Parcelable>());
                break;
            default:
                throw new IllegalArgumentException("In EncodingUtils.encodeChar(): Unsupported " +
                        "value " + value + " encountered");
        }

        return bundle;

		/* TODO: Figure out how to get this working (if it is even possible)
		case :
			dataPacket.putParcelable(key, new Parcelable() {

				@Override
				public int describeContents()
				{
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public void writeToParcel(Parcel dest, int flags)
				{
					// TODO Auto-generated method stub

				}});
			break;

		// NOTE: short and short-arrays can be retrieved as serializables
		case 'v':
			dataPacket.putSerializable(key, new Serializable(){});
			break;
		*/
    }

    public static boolean containsExpansionCode(Bundle dataPacket, String key) {
        // TODO: Move into concrete EncodingScheme classes (need different version for all three mechanisms)

        boolean containsExCode = (dataPacket.getBundle(key) != null);
        //Log.d(TRACE_TAG, "Key \"" + key + "\" maps to an expansion code value: " + containsExCode);
        return containsExCode;
    }

    /**
     * Note: This function assumes that any expansion code has already been extracted.
     *
     * @param dataPacket
     *@param key
     * @param buildVersion   @return
     */
    // TODO: change to decodeValue
    public static int decodeValueForEntry(Bundle dataPacket, String key, int expansionCode, int buildVersion) {
        //Log.d(TRACE_TAG, "Decoding value for key \"" + key + "\"");

        /**
         Notes and TODO's:
         --------------------------------------------------------------------------------

         // TODO: Figure out if there is a better way to do this (maybe request
         // specific ArrayList types to allow encoding schemas to vary which arraylist
         // type is is used to enable the schemas to have more control over their
         // signatures
         //
         // All ArrayList types are treated as being equivalent by the Bundle
         // get-by-type methods and as such on the CharSequence ArrayList type
         // is used for encoding data
         //
         // Note: getShort() and getShortArray() are distinct, but both will
         // cause getSerializable() to return a non-null value
         //

         --------------------------------------------------------------------------------
         * Additional Possible Data Types to Use:
         --------------------------------------------------------------------------------

         if(charCodeBundle.getString(key) != null) {
         c = 'z';
         }

         // Note that getSerializable()  non-null if either the short
         // or short array is set
         else if(charCodeBundle.getSerializable(key) != null) {
         c = 'v';
         }

         else if(charCodeBundle.getIntegerArrayList(key) != null) {
         charCode = 15;
         }

         else if(charCodeBundle.getParcelableArrayList(key) != null) {
         charCode = 19;
         }

         else if(dataPacket.getParcelable(key) != null)
         {
         c = 's';
         }
         */

        int charSetSize = getCharacterSetSize(buildVersion);

        int charCode;
        if(dataPacket.getBoolean(key)) {
            charCode = 0;
        } else if(dataPacket.getBooleanArray(key) != null) {
            charCode = 1;
        } else if(dataPacket.getByte(key) != 0) {
            charCode = 2;
        } else if(dataPacket.getByteArray(key) != null) {
            charCode = 3;
        } else if(dataPacket.getChar(key) != 0) {
            charCode = 4;
        } else if(dataPacket.getCharArray(key) != null){
            charCode = 5;
        } else if(dataPacket.getCharSequence(key) != null) {
            charCode = 6;
        } else if(dataPacket.getCharSequenceArray(key) != null) {
            charCode = 7;
        } else if(dataPacket.getCharSequenceArrayList(key) != null) {
            charCode = 8;
        } else if(dataPacket.getDouble(key) != 0.0) {
            charCode = 9;
        } else if(dataPacket.getDoubleArray(key) != null) {
            charCode = 10;
        } else if(dataPacket.getFloat(key) != 0.0) {
            charCode = 11;
        } else if(dataPacket.getFloatArray(key) != null) {
            charCode = 12;
        } else if(dataPacket.getInt(key) != 0) {
            charCode = 13;
        } else if(dataPacket.getIntArray(key) != null) {
            charCode = 14;
        } else if(dataPacket.getLong(key) != 0) {
            charCode = 15;
        } else if(dataPacket.getLongArray(key) != null) {
            charCode = 16;
        } else if(dataPacket.getParcelableArray(key) != null) {
            charCode = 17;
        } else if(dataPacket.getShort(key) != 0) {
            charCode = 18;
        } else if(dataPacket.getShortArray(key) != null) {
            charCode = 19;
        } else if(dataPacket.getSparseParcelableArray(key) != null) {
            charCode = 20;
        } else {
            throw new IllegalArgumentException("In EncodingUtils.decodeValueForEntry():" +
                    " No value could be decoded for the given key (" + key + ")");
        }

		// Applies the effects of the expansion code
		charCode += (charSetSize * expansionCode);

        //Log.d(TRACE_TAG, "Decoded char code of " + charCode + " for key " + "\"" + key + "\"");

		return charCode;
	}
	
    /**
     * Method for reading data from a text resource file in the res/raw/ folder
     * based on code found at
     * http://stackoverflow.com/questions/4087674/android-read-text-raw-resource-file
     */
    public static String readRawTextFile(Context ctx, int resId, int charsToRead)
    {
        InputStream inputStream = ctx.getResources().openRawResource(resId);
        
        try
        {
	        inputStream.reset();
        } 
        catch (IOException e1)
        {
	        // TODO Auto-generated catch block
	        e1.printStackTrace();
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int i, charsRead = 0;
        try 
        {
        	i = inputStream.read();
        	while (i != -1 && charsRead < charsToRead)
        	{
        		outputStream.write(i);
            	i = inputStream.read();
        		++charsRead;
            }
        	
        	inputStream.close();
        }
        catch (IOException e)
        {
        	return null;
        }
          
        String retString = outputStream.toString();
        
        try
        {
	        inputStream.close();
        } 
        catch (IOException e)
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        
        try
        {
	        outputStream.close();
        } 
        catch (IOException e)
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        
        return retString;
    }

    // TODO: Cleanup
    public static double getTimeMillisAccurate() {
    //public static long getTimeMillisAccurate() {
        // TODO: Cleanup
        return Math.round(Math.abs(System.nanoTime()) / 1000000);
        //return Math.abs(System.nanoTime()) / 1000000.0;
        //return System.currentTimeMillis();
    }
}
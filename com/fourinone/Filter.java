package com.fourinone;

import java.util.Date;
import java.lang.reflect.Array;

public interface Filter<T,S> extends CoolHashBase{
	T getFilterKey();
	S[] getFilterValue();
	boolean match(byte[] vb);
	
	public class ValueFilter<T,S> implements Filter<T,S>{
		private T filterKey;
		private S[] filterValue;
		Condition valuecd;
		
		ValueFilter(T filterKey, S filterValue) throws CoolHashException{
			this.filterKey=filterKey;
			this.filterValue=(S[])Array.newInstance(filterValue.getClass(), 1);
			this.filterValue[0]=filterValue;
			defineValueException();
		}
		
		ValueFilter(T filterKey, S fromObj, S toObj) throws CoolHashException{
			this.filterKey=filterKey;
			this.filterValue=(S[])Array.newInstance(fromObj.getClass(), 2);
			this.filterValue[0]=fromObj;
			this.filterValue[1]=toObj;
			defineValueException();
		}
		
		ValueFilter(T filterKey, S[] filterValue) throws CoolHashException{
			this.filterKey=filterKey;
			this.filterValue=filterValue;
			defineValueException();
		}
		
		public T getFilterKey(){
			return filterKey;
		}
		public S[] getFilterValue(){
			return filterValue;
		}
		
		public static <T extends Number> Filter greater(T targetObj) throws CoolHashException{
			return new ValueFilter<String, T>(Condition.Action.OP_GREATER.name(), targetObj);
		}
		
		public static <T extends Number> Filter less(T targetObj) throws CoolHashException{
			return new ValueFilter<String, T>(Condition.Action.OP_LESS.name(), targetObj);
		}
		
		public static <T extends Number> Filter equals(T targetObj) throws CoolHashException{
			return new ValueFilter<String, T>(Condition.Action.OP_EQUALS.name(), targetObj);
		}
		
		public static <T extends Number> Filter notEquals(T targetObj) throws CoolHashException{
			return new ValueFilter<String, T>(Condition.Action.OP_NOTEQUALS.name(), targetObj);
		}
		
		public static <T extends Number> Filter between(T fromObj, T toObj) throws CoolHashException{
			return new ValueFilter<String, T>(Condition.Action.OP_BETWEEN.name(), fromObj, toObj);
		}
		
		public static <T extends Number> Filter betweenEquals(T fromObj, T toObj) throws CoolHashException{
			return new ValueFilter<String, T>(Condition.Action.OP_BETWEEN_EQUALS.name(), fromObj, toObj);
		}
		
		public static Filter earlier(Date targetDate) throws CoolHashException{
			return greater(targetDate.getTime());
		}
		
		public static Filter later(Date targetDate) throws CoolHashException{
			return less(targetDate.getTime());
		}
		
		public static Filter same(Date targetDate) throws CoolHashException{
			return equals(targetDate.getTime());
		}
		
		public static Filter notSame(Date targetDate) throws CoolHashException{
			return notEquals(targetDate.getTime());
		}
		
		public static Filter during(Date fromDate, Date toDate) throws CoolHashException{
			return between(fromDate.getTime(), toDate.getTime());
		}
		
		public static Filter duringInclude(Date fromDate, Date toDate) throws CoolHashException{
			return betweenEquals(fromDate.getTime(), toDate.getTime());
		}
		
		public static Filter equalsString(String targetObj) throws CoolHashException{
			return new ValueFilter<String, String>(Condition.Action.OP_EQUALS_STR.name(), targetObj);
		}
		
		public static Filter notEqualsString(String targetObj) throws CoolHashException{
			return new ValueFilter<String, String>(Condition.Action.OP_NOTEQUALS_STR.name(), targetObj);
		}
		
		public static Filter startsWith(String targetObj) throws CoolHashException{
			return new ValueFilter<String, String>(Condition.Action.OP_STARTSWITH.name(), targetObj);
		}
		
		public static Filter endsWith(String targetObj) throws CoolHashException{
			return new ValueFilter<String, String>(Condition.Action.OP_ENDSWITH.name(), targetObj);
		}
		
		public static Filter contains(String targetObj) throws CoolHashException{
			return new ValueFilter<String, String>(Condition.Action.OP_CONTAINS.name(), targetObj);
		}
		
		public static Filter beContained(String targetObj) throws CoolHashException{
			return new ValueFilter<String, String>(Condition.Action.OP_BECONTAINED.name(), targetObj);
		}
		
		public static Filter matches(String regex) throws CoolHashException{
			chex.checkingRegex(regex);
			return new ValueFilter<String, String>(Condition.Action.OP_MATCHES.name(), regex);
		}
		
		private void defineValueException() throws CoolHashException{
			for(int i=1;i<filterValue.length;i++)
				if(!filterValue[0].getClass().equals(filterValue[i].getClass()))
					throw new CoolHashException("the parameter object class type are not same!");
			chex.checkTargetMatch(filterValue[0].getClass());
			valuecd=ConstantBit.Target.getTargetMatch(filterValue[0].getClass());
		}
		
		public boolean match(byte[] vb){
			return valuecd.matchAction(this, vb);
		}
		
		public static void main(String[] args){
		}
	}
	
	interface Condition<T,S> {
		boolean matchAction(T key, S value);
		enum Action implements Condition{
			OP_GREATER{
				public boolean matchAction(Object key, Object value){
					return (((Number)key).doubleValue()>(((Number[])value)[0]).doubleValue());
				}
			},
			OP_LESS{
				public boolean matchAction(Object key, Object value){
					return (((Number)key).doubleValue()<(((Number[])value)[0]).doubleValue());
				}
			},
			OP_EQUALS{
				public boolean matchAction(Object key, Object value){
					return (((Number)key).doubleValue()==(((Number[])value)[0]).doubleValue());
				}
			},
			OP_NOTEQUALS{
				public boolean matchAction(Object key, Object value){
					return (((Number)key).doubleValue()!=(((Number[])value)[0]).doubleValue());
				}
			},
			OP_GREATER_EQUALS{
				public boolean matchAction(Object key, Object value){
					return (((Number)key).doubleValue()>=(((Number[])value)[0]).doubleValue());
				}
			},
			OP_LESS_EQUALS{
				public boolean matchAction(Object key, Object value){
					return (((Number)key).doubleValue()<=(((Number[])value)[0]).doubleValue());
				}
			},
			OP_BETWEEN{
				public boolean matchAction(Object key, Object value){
					return (((Number)key).doubleValue()>(((Number[])value)[0]).doubleValue()&&((Number)key).doubleValue()<(((Number[])value)[1]).doubleValue());
				}
			},
			OP_BETWEEN_EQUALS{
				public boolean matchAction(Object key, Object value){
					return (((Number)key).doubleValue()>=(((Number[])value)[0]).doubleValue()&&((Number)key).doubleValue()<=(((Number[])value)[1]).doubleValue());
				}
			},
			OP_EQUALS_STR{
				public boolean matchAction(Object key, Object value){
					return ((String)key).equals(((String[])value)[0]);
				}
			},
			OP_NOTEQUALS_STR{
				public boolean matchAction(Object key, Object value){
					return !((String)key).equals(((String[])value)[0]);
				}
			},
			OP_STARTSWITH{
				public boolean matchAction(Object key, Object value){
					return ((String)key).startsWith(((String[])value)[0]);
				}
			},
			OP_ENDSWITH{
				public boolean matchAction(Object key, Object value){
					return ((String)key).endsWith(((String[])value)[0]);
				}
			},
			OP_CONTAINS{
				public boolean matchAction(Object key, Object value){
					return ((String)key).contains(((String[])value)[0]);
				}
			},
			OP_BECONTAINED{
				public boolean matchAction(Object key, Object value){
					return ((String[])value)[0].contains((String)key);
				}
			},
			OP_MATCHES{
				public boolean matchAction(Object key, Object value){
					return ((String)key).matches(((String[])value)[0]);
				}
			};
		};
	}
}


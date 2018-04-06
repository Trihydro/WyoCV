package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.library.tables.TimOracleTables;
import com.trihydro.library.model.ActiveTimItisCode;;

public class ActiveTimItisCodeService extends CvDataServiceLibrary {

	static PreparedStatement preparedStatement = null;
	
    public static Long insertActiveTimItisCode(Long activeTimId, Integer itisCodeId) { 
		try {
            TimOracleTables timOracleTables = new TimOracleTables();
            String insertQueryStatement = timOracleTables.buildInsertQueryStatement("ACTIVE_TIM_ITIS_CODE", timOracleTables.getActiveTimItisCodeTable());		
            preparedStatement = DbUtility.getConnection().prepareStatement(insertQueryStatement, new String[] {"ACTIVE_TIM_ITIS_CODE_ID"});
            int fieldNum = 1;            
			for(String col: timOracleTables.getActiveTimItisCodeTable()) {
				if(col.equals("ACTIVE_TIM_ID")) 
                    SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, activeTimId);														
                else if(col.equals("ITIS_CODE_ID"))
                    SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, itisCodeId);														               								
                fieldNum++;
			}			            
            Long activeTimItisCodeId = log(preparedStatement, "active tim itis code");		 		            
			return activeTimItisCodeId;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {			
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new Long(0);
	}

	public static boolean deleteActiveTimItisCodes(Long activeTimId){
		
		boolean deleteActiveTimItisCodesResult = false;

		String deleteSQL = "DELETE FROM ACTIVE_TIM_ITIS_CODE WHERE ACTIVE_TIM_ID = ?";

		try {			
		
			preparedStatement = DbUtility.getConnection().prepareStatement(deleteSQL);			
			preparedStatement.setLong(1, activeTimId);

			// execute delete SQL stetement
			deleteActiveTimItisCodesResult = updateOrDelete(preparedStatement);

			System.out.println("Record is deleted!");

		}catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				preparedStatement.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}					
		}
		return deleteActiveTimItisCodesResult;
	}

	public static List<ActiveTimItisCode> getActiveTimItisCodes(){
		
		ActiveTimItisCode activeTimItisCode = null;
		List<ActiveTimItisCode> activeTimItisCodes = new ArrayList<ActiveTimItisCode>();

		try {
			Statement statement = DbUtility.getConnection().createStatement();
			ResultSet rs = statement.executeQuery("select * from ACTIVE_TIM_ITIS_CODE");
			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTimItisCode = new ActiveTimItisCode();		
					activeTimItisCode.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTimItisCode.setActiveTimItisCodeId(rs.getLong("ACTIVE_TIM_ITIS_CODE_ID"));	
					activeTimItisCode.setItisCodeId(rs.getLong("ITIS_CODE_ID"));	
					activeTimItisCodes.add(activeTimItisCode);				
				}
			}
			finally {
				try {
					rs.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}					
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return activeTimItisCodes;
	}
}

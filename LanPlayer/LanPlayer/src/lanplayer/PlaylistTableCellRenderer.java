package lanplayer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import utilities.SimpleDate;

public class PlaylistTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8149911584155489952L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        
	        int modelIndex = table.convertRowIndexToModel(row);
	        if(!isSelected && ((ITableModel) table.getModel()).isCurrentlyPlayed(modelIndex)) {
	        	label.setBackground(Color.GREEN);
	        }
	        else if(!isSelected) {
	        	label.setBackground(Color.WHITE);
	        }
	        
	        if(value instanceof String) {
	            label.setToolTipText((String) value);
	            label.setIconTextGap(5);
	            label.setHorizontalAlignment(SwingConstants.LEADING);
	            label.setText(" " + (String) value);
	        }
	        else if(value instanceof Number) {
	            int countDigits = value.toString().length();
	            label.setHorizontalAlignment(SwingConstants.TRAILING);
	            label.setIconTextGap(table.getColumnModel().getColumn(0).getWidth() - (countDigits * 5) - 19);
	            label.setText(value.toString() + " ");
	        }
	        else if(value instanceof SimpleDate) {
	        	 label.setToolTipText(value.toString());
		         label.setIconTextGap(5);
		         label.setHorizontalAlignment(SwingConstants.LEADING);
		         //label.setHorizontalAlignment(SwingConstants.CENTER);
		         label.setText(" " + value.toString());
	        	
	        }
	        else {
	        	if(value != null) {
	        		label.setToolTipText(value.toString());
	        		label.setText(" " + value.toString());
	        	} 
	            label.setIconTextGap(5);
	            label.setHorizontalAlignment(SwingConstants.LEADING);
	            
	        }
	        return label;
	    }
		
	}
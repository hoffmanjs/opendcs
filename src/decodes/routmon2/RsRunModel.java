package decodes.routmon2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import javax.swing.table.AbstractTableModel;

import decodes.db.ScheduleEntryStatus;

public class RsRunModel extends AbstractTableModel
{
	private RSBean activeRS = null;
	String[] colnames = null;
	RoutingMonitorFrame frame = null;
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd-HH:mm:ss");
	
	public RsRunModel(RoutingMonitorFrame frame)
	{
		this.frame = frame;
		colnames = new String[]{ 
			RoutingMonitorFrame.genericLabels.getString("start"), 
			RoutingMonitorFrame.genericLabels.getString("stop"), 
			RoutingMonitorFrame.procmonLabels.getString("runStats"), 
			RoutingMonitorFrame.procmonLabels.getString("lastMsg"),
			RoutingMonitorFrame.procmonLabels.getString("status"),
			RoutingMonitorFrame.procmonLabels.getString("input"),
			RoutingMonitorFrame.procmonLabels.getString("output")
			};
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	@Override
	public int getRowCount()
	{
		return activeRS == null ? 0 : activeRS.getRunHistory().size();
	}

	@Override
	public int getColumnCount()
	{
		return colnames.length;
	}
	
	@Override
	public String getColumnName(int col)
	{
		return colnames[col];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		if (activeRS == null)
			return "";
		ScheduleEntryStatus ses = activeRS.getRunHistory().get(rowIndex);
		switch(columnIndex)
		{
		case 0: return sdf.format(ses.getRunStart());
		case 1: return ses.getRunStop() == null ? "-" : sdf.format(ses.getRunStop());
		case 2: return "" + ses.getNumMessages() + "/" + ses.getNumPlatforms() + "/" + ses.getNumDecodesErrors();
		case 3: return ses.getLastMessageTime() == null ? "-" : sdf.format(ses.getLastMessageTime());
		case 4: return ses.getRunStatus();
		case 5: return ses.getLastSource() == null ? "-" : ses.getLastSource();
		case 6: return ses.getLastConsumer() == null ? "-" : ses.getLastConsumer();
		default: return "";
		}
	}

	public void setActiveRS(RSBean activeRS)
	{
System.out.println("RsRunModel.setActiveRS(" + activeRS.getRsName()+ ")");
		this.activeRS = activeRS;
		this.fireTableDataChanged();
	}

	public void merge(ArrayList<ScheduleEntryStatus> statusList)
	{
	}

	public void updated()
	{
		this.fireTableDataChanged();
	}

}

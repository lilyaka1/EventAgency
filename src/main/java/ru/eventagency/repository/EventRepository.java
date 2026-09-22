package ru.eventagency.repository;

import ru.eventagency.model.*;
import ru.eventagency.util.DatabaseManager;
import java.sql.*;
import java.util.*;

public class EventRepository implements Repository<Event> {
    private final DatabaseManager db;
    public EventRepository(DatabaseManager db) { this.db=db; }
    private Event map(ResultSet rs)throws SQLException{return new Event(rs.getInt("id"),rs.getInt("client_id"),rs.getString("client_name"),rs.getString("title"),EventType.valueOf(rs.getString("event_type")),EventStatus.valueOf(rs.getString("status")),rs.getDate("event_date").toLocalDate(),rs.getBigDecimal("budget"),rs.getString("description"));}
    public Event create(Event e){String sql="INSERT INTO event_orders(client_id,title,event_type,status,event_date,budget,description) VALUES(?,?,?,?,?,?,?) RETURNING id";try(Connection c=db.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,e.getClientId());p.setString(2,e.getTitle());p.setString(3,e.getType().name());p.setString(4,e.getStatus().name());p.setDate(5,java.sql.Date.valueOf(e.getEventDate()));p.setBigDecimal(6,e.getBudget());p.setString(7,e.getDescription());try(ResultSet r=p.executeQuery()){r.next();return new Event(r.getInt(1),e.getClientId(),null,e.getTitle(),e.getType(),e.getStatus(),e.getEventDate(),e.getBudget(),e.getDescription());}}catch(SQLException x){throw new RuntimeException("Ошибка создания заказа: "+x.getMessage(),x);}}
    private String base(){return "SELECT e.*, c.full_name AS client_name FROM event_orders e JOIN clients c ON c.id=e.client_id ";}
    public List<Event> findAll(){return query(base()+"ORDER BY e.event_date", List.of());}
    public Optional<Event> findById(int id){List<Event> a=query(base()+"WHERE e.id=?", List.of(id));return a.stream().findFirst();}
    public void update(Event e){String sql="UPDATE event_orders SET client_id=?,title=?,event_type=?,status=?,event_date=?,budget=?,description=? WHERE id=?";try(Connection c=db.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,e.getClientId());p.setString(2,e.getTitle());p.setString(3,e.getType().name());p.setString(4,e.getStatus().name());p.setDate(5,java.sql.Date.valueOf(e.getEventDate()));p.setBigDecimal(6,e.getBudget());p.setString(7,e.getDescription());p.setInt(8,e.getId());if(p.executeUpdate()==0)throw new RuntimeException("Заказ не найден");}catch(SQLException x){throw new RuntimeException("Ошибка изменения заказа: "+x.getMessage(),x);}}
    public void delete(int id){try(Connection c=db.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM event_orders WHERE id=?")){p.setInt(1,id);if(p.executeUpdate()==0)throw new RuntimeException("Заказ не найден");}catch(SQLException x){throw new RuntimeException("Ошибка удаления заказа: "+x.getMessage(),x);}}
    private List<Event> query(String sql, List<Object> parameters){List<Event> a=new ArrayList<>();try(Connection c=db.getConnection();PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<parameters.size();i++)p.setObject(i+1,parameters.get(i));try(ResultSet r=p.executeQuery()){while(r.next())a.add(map(r));return a;}}catch(SQLException x){throw new RuntimeException("Ошибка чтения заказов: "+x.getMessage(),x);}}
    public List<Event> search(String text){String sql=base()+"WHERE LOWER(e.title) LIKE LOWER(?) OR LOWER(e.description) LIKE LOWER(?) ORDER BY e.event_date";String value="%"+text+"%";return query(sql,List.of(value,value));}
    public List<Event> searchByClient(String text){String sql=base()+"WHERE LOWER(c.full_name) LIKE LOWER(?) ORDER BY e.event_date";return query(sql,List.of("%"+text+"%"));}
    public List<Event> filter(EventStatus status, EventType type){StringBuilder s=new StringBuilder(base()+"WHERE 1=1");List<Object> parameters=new ArrayList<>();if(status!=null){s.append(" AND e.status=?");parameters.add(status.name());}if(type!=null){s.append(" AND e.event_type=?");parameters.add(type.name());}s.append(" ORDER BY e.event_date");return query(s.toString(),parameters);}
    public long count(){return findAll().size();}
}

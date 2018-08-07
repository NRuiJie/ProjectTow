package com.enation.app.shop.mobile.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.base.core.model.Member;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.model.OrderItem;
import com.enation.app.shop.core.order.model.SellBack;
import com.enation.app.shop.core.order.model.SellBackStatus;
import com.enation.app.shop.core.order.service.OrderStatus;
import com.enation.eop.sdk.context.UserConext;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.StringUtil;

@Service
public class ApiOrderManager  {

	@Autowired
	private IDaoSupport daoSupport;
	
    /**
     * 获取指定状态的订单数量
     *
     * @param status
     * @param member_id
     * @return
     */
    public int count(int status, int member_id) {
        String sql = "select COUNT(0) from es_order where parent_id is NOT NULL and member_id=? and disabled=0";
        if (status >= 0) {
            //等待付款的订单 按付款状态查询
            if (status == 0) {
            	sql += " and ( ( payment_type!='cod' and  status=" + OrderStatus.ORDER_CONFIRM + ") ";// 非货到付款的，未付款状态的可以结算
				sql += " or ( payment_type='cod' and   status=" + OrderStatus.ORDER_ROG + "  ) )";// 货到付款的要发货或收货后才能结算
            } else {
                sql += " AND status='" + status + "'";
            }
        }
        return this.daoSupport.queryForInt(sql, member_id);
    }

    /**
     * 取订单列表
     * @param pageNo
     * @param pageSize
     * @param status
     * @param keyword
     * @return
     */
    public Page pageOrders(int pageNo, int pageSize, String status, String keyword){
        Member member = UserConext.getCurrentMember();

        String sql = "select * from es_order where member_id = '" + member.getMember_id() + "' and disabled=0";
        if(!StringUtil.isEmpty(status)){
            int statusNumber = -999;
            statusNumber = StringUtil.toInt(status);
            //等待付款的订单 按付款状态查询
            if(statusNumber==0){
            	sql+=" AND status!="+OrderStatus.ORDER_CANCELLATION+" AND pay_status="+OrderStatus.PAY_NO;
            }else{
                sql += " AND status='" + statusNumber + "'";
            }
        }
        if(!StringUtil.isEmpty(keyword)){
            sql += " AND order_id in (SELECT i.order_id FROM es_order_items i LEFT JOIN es_order o ON i.order_id=o.order_id WHERE o.member_id='"+member.getMember_id()+"' AND i.name like '%" + keyword + "%')";
        }
        sql += " order by create_time desc";
        Page rpage = this.daoSupport.queryForPage(sql,pageNo, pageSize, Order.class);

        return rpage;
    }

    /**
     * 取待评论的商品数
     *
     * @param member_id
     * @return
     */
    public int commentGoodsCount(int member_id) {
        return this.daoSupport.queryForInt("SELECT count(0) FROM es_member_order_item m LEFT JOIN es_goods g ON m.goods_id=g.goods_id WHERE m.member_id=? AND m.commented=0", member_id);
    }

    /**
     * 获取退换货数量
     *
     * @param member_id
     * @return
     */
    public int returnedCount(int member_id) {
        return this.daoSupport.queryForInt("select count(0) from es_sellback_list where member_id=? and tradestatus!=?", member_id,SellBackStatus.refund.getValue());
    }

    /**
     * 获取订单项详情
     *
     * @param item_id
     * @return
     */
    public OrderItem getItem(int item_id) {
        return (OrderItem) this.daoSupport.queryForObject("SELECT * FROM es_order_items WHERE item_id=?", OrderItem.class, item_id);
    }

    /**
     * 根据订单ID获取退货换详情
     * @param order_id
     * @return
     */
    public SellBack getSellBack(Integer order_id) {
        String sql = "select * from es_sellback_list where orderid=? and ( (type=1 and tradestatus!=?) or (type=2 and tradestatus!=?))";
        List<SellBack> SellBacks = (List<SellBack>) this.daoSupport.queryForList(sql, SellBack.class, order_id, SellBackStatus.refuse.getValue(),SellBackStatus.cancel.getValue());
        if(SellBacks != null && SellBacks.size() > 0){
            return SellBacks.get(0);
        }
        return null;

    }


}

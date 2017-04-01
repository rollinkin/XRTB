package com.xrtb.exchanges.google;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.google.openrtb.OpenRtb.BidResponse;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

import com.xrtb.bidder.SelectedCreative;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.common.Creative;

import com.xrtb.pojo.Impression;


/**
 * Build a Protobuf openRTB bid response.
 * @author Ben M. Faul
 *
 */
public class GoogleBidResponse extends com.xrtb.pojo.BidResponse {
	BidResponse internal;
	BidResponse.Builder builder;
	
	/**
	 * Default constructor
	 */
	public GoogleBidResponse() {
		
	}
	
	/**
	 * Create a protobuf based bid response, multiple creative response.
	 * @param br GoogleBidRequest. The Bid request corresponding to this response.
	 * @param imp Impression. The impression belonging to this response.
	 * @param multi List. The list of selected creatives that match.
	 * @param xtime int. The time it took to make the request.
	 * @throws Exception on Protobuf serialization errors.
	 */
	public GoogleBidResponse(GoogleBidRequest br, Impression imp, List<SelectedCreative> multi, int xtime) throws Exception {
		this.br = br;
		this.exchange = br.getExchange();
		this.xtime = xtime;
		this.oidStr = br.id;
		this.impid = imp.impid;
		/** Set the response type ****************/
		if (imp.nativead)
			this.adtype="native";
		else
		if (imp.video != null)
			this.adtype="video";
		else
			this.adtype="banner";
		/******************************************/
		
		/** The configuration used for generating this response */
		Configuration config = Configuration.getInstance();
		StringBuilder snurl = new StringBuilder();
		
		/**
		 * Create the stub for the nurl, thus
		 */
		StringBuilder xnurl = new StringBuilder(config.winUrl);
		xnurl.append("/");
		xnurl.append(br.getExchange());
		xnurl.append("/");
		xnurl.append("${AUCTION_PRICE}"); // to get the win price back from the
											// Exchange....
		xnurl.append("/");
		xnurl.append(lat);
		xnurl.append("/");
		xnurl.append(lon);
		xnurl.append("/");
		
		SeatBid.Builder sbb = SeatBid.newBuilder();
		for (int i=0;i<multi.size(); i++) {
			SelectedCreative x = multi.get(i);
			this.camp = x.getCampaign();
			this.creat = x.getCreative();
			this.price = Double.toString(x.price);
			this.dealId = x.dealId;
			this.adid = camp.adId;
			this.imageUrl = substitute(creat.imageurl);
			snurl = new StringBuilder(xnurl);
			snurl.append(adid);
			snurl.append("/");
			snurl.append(creat.impid);
			snurl.append("/");
			snurl.append(oidStr.replaceAll("#", "%23"));
			snurl.append("/");
			snurl.append(br.siteId);
			
			
			Bid.Builder bb = Bid.newBuilder();
			bb.addAdomain(camp.adomain);
			bb.setAdid(camp.adId);
			bb.setNurl(snurl.toString());
			bb.setImpid(x.impid);
			bb.setId(br.id);
			bb.setPrice(x.price);
			if (dealId != null)
				bb.setDealid(x.dealId);
			bb.setIurl(substitute(imageUrl));
			
			String adm;
			if (br.usesEncodedAdm)
				adm = substitute(creat.encodedAdm);
			else
				adm = substitute(creat.unencodedAdm);
			
			bb.setAdm(adm);
			sbb.addBid(bb.build());
		}
		
		sbb.setSeat(Configuration.getInstance().seats.get(exchange));
		
		SeatBid seatBid = sbb.build();
		builder.addSeatbid(seatBid);
			
		internal = builder.build();
	}
	
	/**
	 * Single response constructor for Google protobuf.
	 * @param br GoogleBidRequest. The bid request that belongs to this response.
	 * @param imp Impression. Not used.
	 * @param camp Campaign. The campaign that is responding.
	 * @param creat Creative. The creative that is responding.
	 * @param oidStr The request object id.
	 * @param price double. The price we are bidding at.
	 * @param dealId String. The deal id.
	 * @param xtime int. The time it took to make the bid.
	 * @throws Exception on protobuf errors.
	 */
	public GoogleBidResponse(GoogleBidRequest br, Impression imp, Campaign camp, Creative creat,
			String oidStr, double price, String dealId, int xtime) throws Exception {
		this.br = br;
		this.imp = imp;
		this.camp = camp;
		this.oidStr = oidStr;
		this.creat = creat;
		this.xtime = xtime;
		this.price = Double.toString(price);
		this.dealId = dealId;

		impid = imp.impid;
		adid = camp.adId;
		crid = creat.impid;
		this.domain = br.siteDomain;

		forwardUrl = substitute(creat.getForwardUrl()); // creat.getEncodedForwardUrl();
		imageUrl = substitute(creat.imageurl);
		exchange = br.getExchange();
		
		builder = BidResponse.newBuilder();
		
		builder.setBidid(br.id);
		
		StringBuilder snurl = new StringBuilder(Configuration.getInstance().winUrl);
		snurl.append("/");
		snurl.append(br.getExchange());
		snurl.append("/");
		snurl.append("${AUCTION_PRICE}"); // to get the win price back from the
											// Exchange....
		snurl.append("/");
		snurl.append(lat);
		snurl.append("/");
		snurl.append(lon);
		snurl.append("/");
		snurl.append(adid);
		snurl.append("/");
		snurl.append(creat.impid);
		snurl.append("/");
		snurl.append(oidStr.replaceAll("#", "%23"));
		
		String adm;
		if (br.usesEncodedAdm)
			adm = substitute(creat.encodedAdm);
		else
			adm = substitute(creat.unencodedAdm);
		
		
		Bid.Builder bb = Bid.newBuilder();
		bb.addAdomain(camp.adomain);
		bb.setAdid(camp.adId);
		bb.setNurl(snurl.toString());
		bb.setImpid(impid);
		bb.setId(br.id);
		bb.setPrice(price);
		if (dealId != null)
			bb.setDealid(dealId);
		bb.setIurl(substitute(imageUrl));
		bb.setAdm(adm);
		
		SeatBid.Builder sbb = SeatBid.newBuilder();
		sbb.addBid(bb.build());
		sbb.setSeat(Configuration.getInstance().seats.get(exchange));
		
		SeatBid seatBid = sbb.build();
		builder.addSeatbid(seatBid);
			
		internal = builder.build();
	}
	
	/**
	 * Write the response This used to transmit the OCTET string back.
	 */
	@Override
	public void writeTo(HttpServletResponse response) throws Exception {
		response.setContentType("application/octet-string");
		internal.writeTo(response.getOutputStream());
	}
	
	/**
	 * Write the response using your favorite type.
	 */
	@Override
	public void writeTo(HttpServletResponse response, String x) throws Exception {
		response.setContentType(x);
		internal.writeTo(response.getOutputStream());
	}
	
	/**
	 * Returns a string representation of the request in Protobuf form.
	 */
	@Override
	public String toString() {
		return internal.toString();
	}
}

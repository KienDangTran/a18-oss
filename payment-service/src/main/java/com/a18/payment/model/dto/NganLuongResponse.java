package com.a18.payment.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import lombok.Getter;

@Getter
@XmlRootElement(name = "result")
public final class NganLuongResponse implements Serializable {

  @XmlElement(name = "error_code")
  private NganLuongError error_code;

  @XmlElement(name = "token")
  private String token;

  @XmlElement(name = "checkout_url")
  private String checkout_url;

  @XmlElement(name = "time_limit")
  private String time_limit;

  @XmlElement(name = "description")
  private String description;

  @XmlElement(name = "receiver_email")
  private String receiver_email;

  @XmlElement(name = "order_code")
  @JsonProperty("ref_code")
  private String order_code;

  @XmlElement(name = "total_amount")
  private String total_amount;

  @XmlElement(name = "payment_method")
  private String payment_method;

  @XmlElement(name = "bank_code")
  private String bank_code;

  @XmlElement(name = "payment_type")
  private String payment_type;

  @XmlElement(name = "order_description")
  private String order_description;

  @XmlElement(name = "transaction_id")
  private String transaction_id;

  @XmlElement(name = "return_url")
  private String return_url;

  @XmlElement(name = "cancel_url")
  private String cancel_url;

  @XmlElement(name = "buyer_fullname")
  private String buyer_fullname;

  @XmlElement(name = "buyer_email")
  private String buyer_email;

  @XmlElement(name = "buyer_mobile")
  private String buyer_mobile;

  @XmlElement(name = "transaction_status")
  private NganLuongStatus transaction_status;

  @XmlType
  @XmlEnum
  public enum NganLuongError {
    @XmlEnumValue("00") E_00("00", "Không có lỗi"),
    @JsonProperty("99") @XmlEnumValue("99") E_99(
        "99",
        "Lỗi không được định nghĩa hoặc không rõ nguyên nhân"
    ),

    @JsonProperty("01") @XmlEnumValue("01") E_01(
        "01",
        "Merchant không được phép sử dụng phương thức này"
    ),

    @JsonProperty("02") @XmlEnumValue("02") E_02(
        "02",
        "Địa chỉ IP của merchant gọi tới NganLuong.vn không được chấp nhận / Thông tin thẻ sai định dạng"
    ),

    @JsonProperty("03") @XmlEnumValue("03") E_03(
        "03",
        "Sai tham số (có tham số sai tên hoặc kiểu dữ liệu) / Thông tin merchant không chính xác"
    ),
    @JsonProperty("04") @XmlEnumValue("04") E_04(
        "04",
        "Tên hàm API do merchant gọi tới không hợp lệ (không tồn tại) / Có lỗi trong quá trình kết nối"
    ),
    @JsonProperty("05") @XmlEnumValue("05") E_05(
        "05",
        "Sai version của API / Số tiền không hợp lệ"
    ),
    @JsonProperty("06") @XmlEnumValue("06") E_06(
        "06",
        "Mã merchant không tồn tại hoặc chưa được kích hoạt / Tên chủ thẻ không hợp lệ"
    ),
    @JsonProperty("07") @XmlEnumValue("07") E_07(
        "07",
        "Sai mật khẩu của merchant / Số tài khoản không hợp lệ"
    ),
    @JsonProperty("08") @XmlEnumValue("08") E_08("08", "Tài khoản người bán hàng không tồn tại"),
    @JsonProperty("09") @XmlEnumValue("09") E_09(
        "09",
        "Tài khoản người nhận tiền đang bị phong tỏa / bank_code không hợp lệ"
    ),
    @JsonProperty("10") @XmlEnumValue("10") E_10(
        "10",
        "Hóa đơn thanh toán không hợp lệ / Số dư tài khoản không đủ để thực hiện giao dịch"
    ),
    @JsonProperty("11") @XmlEnumValue("11") E_11(
        "11",
        "Số tiền thanh toán không hợp lệ / Mã tham chiếu ( ref_code ) không hợp lệ"
    ),
    @JsonProperty("12") @XmlEnumValue("12") E_12(
        "12",
        "Đơn vị tiền tệ không hợp lệ / Mã tham chiếu ( ref_code ) đã tồn tại"
    ),
    @JsonProperty("14") @XmlEnumValue("14") E_14("14", "Function không đúng"),
    @JsonProperty("16") @XmlEnumValue("16") E_16(
        "16",
        "receiver_email đang bị khóa hoặc phong tỏa không thể giao dịch"
    ),
    @JsonProperty("17") @XmlEnumValue("17") E_17("17", "account_type không hợp lệ"),
    @JsonProperty("29") @XmlEnumValue("29") E_29("29", "Token không tồn tại"),
    @JsonProperty("80") @XmlEnumValue("80") E_80("80", "Không thêm được đơn hàng"),
    @JsonProperty("81") @XmlEnumValue("81") E_81("81", "Đơn hàng chưa được thanh toán"),
    @JsonProperty("110") @XmlEnumValue("110") E_110(
        "110",
        "Địa chỉ email tài khoản nhận tiền không phải email chính"
    ),
    @JsonProperty("111") @XmlEnumValue("111") E_111("111", "Tài khoản nhận tiền đang bị khóa"),
    @JsonProperty("113") @XmlEnumValue("113") E_113(
        "113",
        "Tài khoản nhận tiền chưa cấu hình là người bán nội dung số"
    ),
    @JsonProperty("114") @XmlEnumValue("114") E_114(
        "114",
        "Giao dịch đang thực hiện, chưa kết thúc"
    ),
    @JsonProperty("115") @XmlEnumValue("115") E_115("115", "Giao dịch bị hủy"),
    @JsonProperty("118") @XmlEnumValue("118") E_118("118", "tax_amount không hợp lệ"),
    @JsonProperty("119") @XmlEnumValue("119") E_119("119", "discount_amount không hợp lệ"),
    @JsonProperty("120") @XmlEnumValue("120") E_120("120", "fee_shipping không hợp lệ"),
    @JsonProperty("121") @XmlEnumValue("121") E_121("121", "return_url không hợp lệ"),
    @JsonProperty("122") @XmlEnumValue("122") E_122("122", "cancel_url không hợp lệ"),
    @JsonProperty("123") @XmlEnumValue("123") E_123("123", "items không hợp lệ"),
    @JsonProperty("124") @XmlEnumValue("124") E_124("124", "transaction_info không hợp lệ"),
    @JsonProperty("125") @XmlEnumValue("125") E_125("125", "quantity không hợp lệ"),
    @JsonProperty("126") @XmlEnumValue("126") E_126("126", "order_description không hợp lệ"),
    @JsonProperty("127") @XmlEnumValue("127") E_127("127", "affiliate_code không hợp lệ"),
    @JsonProperty("128") @XmlEnumValue("128") E_128("128", "time_limit không hợp lệ"),
    @JsonProperty("129") @XmlEnumValue("129") E_129("129", "buyer_fullname không hợp lệ"),
    @JsonProperty("130") @XmlEnumValue("130") E_130("130", "buyer_email không hợp lệ"),
    @JsonProperty("131") @XmlEnumValue("131") E_131("131", "buyer_mobile không hợp lệ"),
    @JsonProperty("132") @XmlEnumValue("132") E_132("132", "buyer_address không hợp lệ"),
    @JsonProperty("133") @XmlEnumValue("133") E_133("133", "total_item không hợp lệ"),
    @JsonProperty("134") @XmlEnumValue("134") E_134(
        "134",
        "payment_method, bank_code không hợp lệ"
    ),
    @JsonProperty("135") @XmlEnumValue("135") E_135("135", "Lỗi kết nối tới hệ thống ngân hàng"),
    @JsonProperty("140") @XmlEnumValue("140") E_140(
        "140",
        "Đơn hàng không hỗ trợ thanh toán trả góp"
    );

    private final String errCode;

    private final String desc;

    NganLuongError(String errCode, String desc) {
      this.errCode = errCode;
      this.desc = desc;
    }

    public String getErrCode() {
      return errCode;
    }

    public String getDesc() {
      return desc;
    }
  }

  @XmlType
  @XmlEnum
  public enum NganLuongStatus {
    @JsonProperty("00") @XmlEnumValue("00") S_00("00", "Đã thanh toán"),
    @JsonProperty("01") @XmlEnumValue("01") S_01("01", "Đã thanh toán, chờ xử lý"),
    @JsonProperty("02") @XmlEnumValue("02") S_02("02", "Chưa thanh toán");

    private final String statusCode;

    private final String desc;

    NganLuongStatus(String statusCode, String desc) {
      this.statusCode = statusCode;
      this.desc = desc;
    }

    public String getStatusCode() {
      return statusCode;
    }

    public String getDesc() {
      return desc;
    }
  }
}

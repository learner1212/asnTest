package test.asnTest;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.BERTags;
import org.bouncycastle.asn1.DERApplicationSpecific;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERUTF8String;

public class BillFile extends ASN1Object {
    List<String[]> headerStruct;
    List<String[]> rcdStruct;
    List<String[]> endStruct;
    List<String> headerValues;
    List<List<String>> rcdValues;
    List<String> endValues;
    private final int headerTag = 33;
    private final int endTag = 34;
    private final int bodyTag = 32;
    int rcdTag;

    public BillFile(String item) {
        ReadConf.getConf(item, this);
    }

    public void setValues(List<String> headerValues, List<List<String>> rcdValues, List<String> endValues) {
        this.headerValues = headerValues;
        this.rcdValues = rcdValues;
        this.endValues = endValues;
    }

    public void setRcdValues(List<List<String>> rcdValues) {
        this.rcdValues = rcdValues;
    }

    @Override
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector root = new ASN1EncodableVector();

        try {
            // ==================================head=============================
            if (headerValues != null && headerStruct != null) {
                ASN1EncodableVector header = getEncodableVector(headerStruct, headerValues);
                root.add(new DERApplicationSpecific(headerTag, header));
            }

            // ==================================body==============================
            if (rcdValues != null && rcdStruct != null) {
                ASN1EncodableVector body = new ASN1EncodableVector();
                for (List<String> rcdValue : rcdValues) {
                    ASN1EncodableVector vector = getEncodableVector(rcdStruct, rcdValue);
                    body.add(new DERApplicationSpecific(rcdTag, vector));
                }
                root.add(new DERApplicationSpecific(bodyTag, body));
            }

            // ==================================end===============================
            if (endValues != null && endStruct != null) {
                ASN1EncodableVector end = getEncodableVector(endStruct, endValues);
                root.add(new DERApplicationSpecific(endTag, end));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new DERApplicationSpecific(1, root);
    }

    public void parse(InputStream in) throws IOException {
        ASN1StreamParser stream = new ASN1StreamParser(in);
        DERApplicationSpecific root = (DERApplicationSpecific) stream.readObject();
        if (root.getApplicationTag() != 1) {
            throw new IOException("error");
        }
        ASN1Sequence seq = (ASN1Sequence) root.getObject(BERTags.SEQUENCE);

        if (seq.size() == 1) { // 没有头记录和尾记录
            DERApplicationSpecific body = (DERApplicationSpecific) seq.getObjectAt(0);
            getBodyValues(body);
            return;
        }

        // ==================================head=============================
        DERApplicationSpecific header = (DERApplicationSpecific) seq.getObjectAt(0);
        ASN1Sequence headerEntry = (ASN1Sequence) header.getObject(BERTags.SEQUENCE);
        headerValues = getValuesFromSeq(headerEntry, headerStruct);

        // ==================================body==============================
        if (seq.size() == 3) {
            DERApplicationSpecific body = (DERApplicationSpecific) seq.getObjectAt(1);
            getBodyValues(body);
        }

        // ==================================end===============================
        DERApplicationSpecific end = (DERApplicationSpecific) seq.getObjectAt(seq.size() == 2 ? 1 : 2);// 如果body为空，end为第二个部分
        ASN1Sequence endEntry = (ASN1Sequence) end.getObject(BERTags.SEQUENCE);
        endValues = getValuesFromSeq(endEntry, endStruct);
    }

    private void getBodyValues(DERApplicationSpecific body) throws IOException {
        ASN1Sequence rcds = (ASN1Sequence) body.getObject(BERTags.SEQUENCE);
        @SuppressWarnings("unchecked")
        Enumeration<DERApplicationSpecific> e = rcds.getObjects();
        this.rcdValues = new ArrayList<>();
        while (e.hasMoreElements()) {
            rcdValues.add(getValuesFromSeq((ASN1Sequence) e.nextElement().getObject(BERTags.SEQUENCE), rcdStruct));
        }
    }

    private List<String> getValuesFromSeq(ASN1Sequence seq, List<String[]> struct) throws IOException {
        Enumeration<DERApplicationSpecific> e = seq.getObjects();
        List<String> list = new ArrayList<>(struct.size());
        for (int i = 0; e.hasMoreElements() && i < struct.size(); i++) {
            String tmp[] = struct.get(i);
            String type = tmp[0];
            if ("String".equalsIgnoreCase(type)) {
                list.add(((DERUTF8String) (e.nextElement()).getObject(BERTags.UTF8_STRING)).getString());
            } else if ("int".equalsIgnoreCase(type)) {
                list.add(String.valueOf(((DERInteger) (e.nextElement()).getObject(BERTags.INTEGER)).getValue()));
            } else {
                throw new IOException("type " + type + " not support!");
            }
        }
        return list;
    }

    private ASN1EncodableVector getEncodableVector(List<String[]> struct, List<String> values) throws IOException {
        ASN1EncodableVector vector = new ASN1EncodableVector();
        for (int i = 0; i < values.size() && i < struct.size(); i++) {
            String[] tmp = struct.get(i);
            String type = tmp[0];
            // int len = Integer.valueOf(rcds[1]);
            int tag = Integer.valueOf(tmp[2]);
            if ("String".equalsIgnoreCase(type)) {
                vector.add(new DERApplicationSpecific(false, tag, new DERUTF8String(values.get(i))));
            } else if ("int".equalsIgnoreCase(type)) {
                vector.add(new DERApplicationSpecific(false, tag, new DERInteger(new BigInteger(values.get(i)))));
            } else {
                throw new IOException("type " + type + " not support!");
            }
        }
        return vector;
    }

}

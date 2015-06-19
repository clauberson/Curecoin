/*
 * Curecoin 2.0.0a Source Code
 * Copyright (c) 2015 Curecoin Developers
 * Distributed under MIT License
 * Requires Apache Commons Library
 * Supports Java 1.7+
 */

import java.io.*;
import java.util.*;
/**
 * This class handles or manages ALL database-related activity, including
 * account balance lookups, account tree management, blockchain storage, etc.
 */
public class CurecoinDatabaseMaster
{
    private File dbFolder;
    private MerkleAddressUtility merkleAddressUtility;
    private Blockchain blockchain;

    //Test method, obviously
    public static void main(String[] args)
    {
        CurecoinDatabaseMaster dbMaster = new CurecoinDatabaseMaster("test");
    }

    /**
     * Attempts to add a block to the blockchain; passthrough to Blockchain.addBlock(block).
     * 
     * @param block Block to add
     * 
     * @return boolean Whether block addition was successful
     */
    public boolean addBlock(Block block)
    {
        //blockchain.tryBlockQueue();
        return blockchain.addBlock(block, false);
    }

    /**
     * Gets a the block at a certain height of the longest chain; passthrough to Blockchain.getBlock(int blockNum).
     * 
     * @param blockNum Number of requested block
     * 
     * @return Block The requested block
     */
    public Block getBlock(int blockNum)
    {
        return blockchain.getBlock(blockNum);
    }

    /**
     * Passthrough to Blockchain.getBlockchainLength()
     * 
     * @return int Length of longest blockchain
     */
    public int getBlockchainLength()
    {
        return blockchain.getBlockchainLength();
    }

    /**
     * Returns the block of the highest chain
     */
    public Block getLatestBlock()
    {
        return getBlock(getBlockchainLength() - 1);
    }

    /**
     * Passthrough to Blockchain.getDifficulty()
     * 
     * @return long Current difficulty on longest chain
     */
    public long getDifficulty()
    {
        return blockchain.getDifficulty();
    }

    /**
     * Constructor for CurecoinDatabaseMaster reads in blockchain data, and writes the first two blocks of the network to a file if necessary.
     */
    public CurecoinDatabaseMaster(String dbFolder)
    {
        this.dbFolder = new File(dbFolder);
        if (!this.dbFolder.exists())
        {
            try
            {
                this.dbFolder.mkdir();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        merkleAddressUtility = new MerkleAddressUtility();
        this.blockchain = new Blockchain(dbFolder);
        File blockchainFile = new File(dbFolder + "/blockchain.dta");
        try
        {
            if (!blockchainFile.exists())
            {
                PrintWriter out = new PrintWriter(blockchainFile);
                //The beautiful genesis block
                out.println("{1433745705903:0:0000000000000000000000000000000000000000000000000000000000000000:150000:829093},{0000000000000000000000000000000000000000000000000000000000000000},{},{C5KKYT7AURETVHAKJ2G2RXNVMA4BKH4KQWCJOW:GENESIS_BLOCK:10000000:CureLabs:0:0000000000000000000000000000000000000000000000000000000000000000},{b2C5yG2YPyyPYl8t:0Xh6tcBOxFspZbWVIdnA::g3Dp0sEDK79brzFU:4293bZre28iGqQMu5XcV::JdRROhKSlAE9q0GcyRmB:DMgbc0MazBJYDqI2::J0wLiB0GWzjsSTAwXlzS:wKP/4GUVAidQkFPD::91oCUJyZO6kz95xDpVqW:k1ZqjCoPw7fyrSHk::LfdlD8ASPjJwLLKp7Y0t:QbYsXRg2Xoyp62xP::RKpaqYzAH46p8NfHsj2W:3rvY2Gumv6RhW9Ts::xP3KMtTSGyCVX9ea:pKuBrrfIoIqk2nhEk0iY::CIdDykApHASDinpHUclO:KFE8ES2GfpSxqZVn::HNZ7cnAgzJmVcHkT:tAjRsrAUnP1LgiKkyOe7::kngEIhhbaBwk1goiKjFc:m/hgnslXSIiOVz34::0cqyM6hFO720j65VnZdh:d1fwqsFT8o+4a93v::tAmAuXeJbKGdidh4:7dYL6BQHoHha5lsm05mv::9cxGGhudabTOadGi3uDo:KaScq263leES6KuM::awR8lBnKZYxdyZCpxBy3:kMT2WsrOcKFp+S4C::0yP7OnSvsScxIVy5:XwnmaTphRMi7gIK30Lw0::YYgSKHjMRpVxiqkd:AUrXOQdirGhd7LrJsNAS::REm/IkfjLgkxjsL/:oy01eofSurDJV5isLek9::j0ncGOras9DauZr3jmyx:PQLExKlbn5kimCJg::iI0IuNedAl3G3ZJOUQZA:e+i8uJkzlZqgGSdU::49hhH9Vg7eppKDhj:TpVwe3rnscwzbZejtUJA::9BUak5gtrahFg9gW:DcLbX14YFqX05JdUk9RQ::xYmiwiPA3HEMoaaVQBDt:n9V0RrYeHK4VC2BB::Ck3saqHbo4I0HVSOgFN5:jf8gkR/wtgR/LhJL::qqsy9kmoZFPLnPWoaYnh:KkTFcTFokMUripc0::dJTKH7EjiBDmVm81LZ1Z:IiWX/G4hHQJsTnUa::CXRoHW1QNM0ojJpzOpMB:dpv8iSxfZ5wsitTO::cnNBtCnIQnEZFZYeOKIr:pzxgAVfQw35A7XV1::BKTK0CBhQ0S2OWMgBFYW:4raBjLH1NxpDGPcy::u8sG+MFtPiCwmtni:lOyV1PuCUwTrtAFCfbFu::TQK3nKZdYq3SNj2oiWnz:hRm5aL/kOMJ/1P9D::ncSL6bWQYACigfih1fZ2:08JK4WmBhmwUpyxG::ydmGr/24tRQ6Cb3Q:XBSGbtCIqtZogcBRiJwi::kmHMtVNrirh1lQSd7q4Z:pqAuglH4UNYBNkn2::5S41Ruqdr8TOnQiATsJQ:vgrMjdNP1+BoMaAE::Y8fYRO2d2f5N5k52:Limx5hD9EZnWDkyDp476::+blNUfADGZ83dUP6:QMb8MiI1tTx8OQRpcPTH::pEffwdG6MWq49q1G:xY4p1CPinff3FYMHl6wh::jIUrAd+LCe4+4zVj:HRukgVOIdmjkCPSFmfO1::J9it8nL+urwFPRoH:rHKqJp7AxRlQ5SCkFYOC::z8eAnwCgy9Sy1J6xXCOW:WXxTQDAFXNxC9TiJ::z6hYmudiiZVObAE0:c8YOXfkTTsPveBU0Svvp::dfmjTIuSe/TE/qr+:X3j2IEoYH8Qfe5SZwUO5::qFO9Odqb05ih2VnC:PRGdC3OBaGAHyINUXyTT::H2l7ciRfVsXOSEQPaAl7:amUCWHL2MUdOVYii::Rl92xzZtm15edhCF:LyrmNkduERpaEQ7RtXTc::BBL9Yees9fHYZPlmOykR:bGupcgyz/7JYV/R9::ePS1EjLUO7d/6W0K:39W6wQDowauvktCDdjDR::obIiuqwKFsg9DWNa50pW:sMEjNmHKm9cxtSyz::IUmrwdfaFn/g/djO:nH6jtD5GsuKcTSb6aMaJ::7mZ2MyI6lChdoSJsc1Vd:sYWq2iTzX2Cvrv37::nSKnkW8PMOlSrJdLIqzg:ezdd9upySy1mKnq1::A9aA0bDP2bhRYfw/:rpc4popZrV0SzAIKWzhV::Vx3ibrnJyCr2OPe5epsn:IYw9kVjYsyM1DME5::GeUUNWhpdAhDKe78H6hq:J6e74Tp4Jo3RZVJl::JaSxPLQLd9xphOformyh:RU/V6Ik3O6ZB6ut9::M+YeBqSCnmlUNQL/:8rRdYARKmpiFDZY8VD3p::t2a4U09AgFgSi/Lr:CcnV65rs4aox9ntaz8lh::uAgWtodZYsAlm06LHAcz:3rEUXUYlx1PIOQ9g::FWdDME2ViwtcjyEUO5oq:8dk7uCdBMtmwzSzO::ECxKglhm3r+YCxor:icGe1LUuPlMUQyFtjBAC::pRPhAiTPSe799o4s:aWNUpcNXvVPH3umiyNG8::mdReqExHyeKedqH6:HbvhoZEefoxnDOqvEyWL::I7hG3xmyIBaF2XppI4dO:glgCri7eSngqdM0b::bbTGj3z4Acog8rgQ:bf0gz2Ygp5skI5K1PHJh::Q7I66mMhk81bxDnzvDJn:cBoHWYbVyOrf3EcV::xbVfPAN4LV432dHht7b4:uMsD67T7TQdj3p6P::od1yUjoRlR7Dyllj3XJy:iYpFeeF1OONgPObq::x9QmfBqVG+sYVhpc:KqJsfOIOhk5qHDTiMPQX::+UiyJLeRaheu4iLR:hGxkwphtQZX2e2FR6M00::EVsn1ab7ywB0f8O7OsRY:MBhMAn+lp+shm4hp::JxfnlbT8Ld6KJRou:78NbPbJ1eb2x2cgIVjgn::rB8Xf9j8TZaEbzbYZ6Yz:+1cSU8U+U5IXHceZ::Xd8fqF6acxlei3E2:5czbetGSzcE6aQGTQdSw::z538bh/E1KY8dJKd:0LRT9D9W1iz7XxsXYwsZ::lIA3RfaJpULUTvoxP25W:2QZBiS/7WDk7ewx4::Z3+dXHZAwix9pvky:rRsSfyy8fd8FlnklLLj9::+xEXP9a6wBgy2xb8:4HSOYTRVvFnmBKpeyoLm::MfVgb6QIroDwt3YjCGFQ:TItGaDe1LTocZ3zm::HwC/VhSZcBmlu6JC:XAtm0k70Ytbdj8UVUOqM::MhrKh4SqEvarAxGmFVCZ:UHXt3PIJ/0FRJxGa::hOeu5aQNiBYcB0IBsbxN:rRHcXptDKlID8C1+::wjvvKqSP1Gp2zv2ScXcO:VONVCyhqhVrat//B::foBCOngWrZHMbza+:6xRO52gnZE3cDW2jKkPk::UihSvjlnzBpbY/FR:qxFqnvEFr1dXXtzuxeeb::UEgJM2y4jdrR0vs8lxql:4WGCRd7YntUQOb0/::o90B+hp33HjGZm3q:PXhx9hZ1OXR7qw4wVwG8::6meja9fNP2lu8WnRwRaO:FgzTRi0R1+alMcxN::8wQJjW50FvIkhrtI:JQUf2Pjh0kBA5sjvqx4C::xugfwSvUWs0j4hRR:mq6z8VdIgNy6k1PTCyQ4::uEw5C7klWKTJCeeokI0A:hxym2RaXyYlYgtia::8zOL94kdFnJKWr3r:i0yu5yDahcBDo760yJZM::xJwvPJIeKsMEl/8Q:DJcG373gujZMI1wvcYAQ::m8tb5saeEXMsqkZK:Rxj0msCbUoj9ZgRzlofF::4hwzgLGze6PbzrttEqDx:k+0JlJ1a59diQAGa::ivTQ2STf6pY62sOi:wJrvXvgCchtWFs40ffb1::pyKfMIJ6OauyApDY:J1uRgMi8CHCzer7Uv0VS::ttZY7ZwZtW95o9Gth4Xc:BRBu98v9vGkxarD3::ygnIsX8GtbMPR7xk:q2rHVLc7GiWGJmLfMZGf::TrCZyW986l8YjWxowQfHuWKJyqBYQlP3uby6jAbXplm6nnSMrIMY5mn5woU1U7u0FkMnxT34skXSmQU/4NNB1g==:qgLyYDa4Kby16Uq48HOv,bqxkI962I2wFe8SgR0zobZBoFT1q158jXrkjB7AiLfI=:EAioAcR7eP/rQzqzp9WhRqZ0thWoq2Nd2MFezgZrgC8=:vU+/EILhNkIrY8e/lhccPR/IisHIFni9+efQSMuVvvQ=:C7NYuk1zAechmHQd3d3+sOhJsJBVWlFoaIAYDuhFCVA=:JC4euxAtKRUnKQwCGz2VIepm8BkpcNwyCbnKFT972Wc=:5z/r+i998uJGObP7Q2eZNH6s27yLdnrQ2Jw6B8E5qCk=:ZcrKFOngJZXxO6I8zWnBpqRvXap7lwlavh3TJUKp/cg=:DM4dvareR4N0+XFdIKJzYjV4f17KM/1rCP/48rlu9sE=:04zBfdP+5lIFilstGKqLpQXP+s5cnNXIwXX+unTe6qs=:nQTxUC7G+Dm8Xu5cdrO/WAJKNoBscKtlNof24emrZVk=:Jh3RHp8+r8fBsUuFlbDZAONatBNgVvqndrXf6uPrIgc=:1DO9Se2lLQtvgrAhOOMAgWUwxbOFMIIcXZKBeQtzoqs=:ykOgoDZGiFtNyMein2njVM19obIohjOdCM/HAwuQa7U=:VjvhVqrVlcSeOd0QR0kc59r86m1LNIEWWAh9cfTMDA8=:OgWfcKIq5OuGKi++W6QsRhPbbUel8tgz/DJZqnfhKx4=:1ckSqYomP2MeTktHD0CM0U0H5esFOEJTjMLflArDAtU=:QXRlbpthSYwGyU/SUnLFpdOQ+ciax0uDVQWe1lGnTOc=:kgPYvTDml7pAq/7XCNqv9kfydccRH4ObdMU0uAupJek=:WTyuno8Sgfhu/TWy4VfyO40hK+0k12rmrpnUJJ2hU6Y=},{0},{4F5BFD86FD26F234F31694954DD2372F2045F47E02B346EB7C50C259E0D86A64},{JfIpaDVNOogVcOmK:4zbCtKIvjMkqFQqAyzQN::oIlsCvn4FEw3LNGskZYS:5k6Uqq/6YWWpyCkH::zqWDXqQOgrbo41l4svBh:U2/ou4jQ2CHAWJ1R::O6swBeM3ZUvwzlwMkGOQ:FTX7chhNMeYQthCt::8oy8nYkhbQTlkiUlR4eW:92aNjag6lhA5jwlI::eMB8TthcHDH+ZggW:K3BobhduECTKgbq5Tyw7::Oxuv7BK5DrYMEgMrqtkK:KrwlF13YlqBw9ruS::F1/017uqnT+w8kd8:MiNI3UO44SbgsO7aLzTC::jzy7lKDqiP1WnL3ZMyTX:1IWzfiH1Kt1xxaDE::d1jhd8SSx6pJ/lvr:2UGDVN6Kw0tK54RX5KpS::WfEQw5u3oC0rRERlQGLx:ii3mtMS250DBr1rU::q7StZ5KID1EsRIR+:9bJurEFvdOruyJsbQonb::qOv0x30GDqP9bqweLPLP:ypFKrMytWQKc5kjN::osVLc/wuD464cx//:Gk0mfcbqva1ZEijoBv22::7zNcfr2pDwScN2Xc:tR5ftmzHKG7SPqLna1UU::ZSfn1pYhM88WUqe3seay:H9AQ03M9z/SBucXj::o9InH6vLymYpb0rFcHeD:jgsUH+3X9zy7OBdF::SWpbGnIxPXsZfuLR:gt9YEgJeUEKm9hGQXoHI::1ECthgzQDjED3EUp:WZqn51OKImT5zmELCgBA::ZVpjPkiVvSaEwUa8KlDi:sSvXLNyq3gYVupbg::R+mMGU4MJ3lP17EW:je2OUshJYyT2U1XDHfVs::mX5oT1ZVmlYcbca4O4F2:rwjIXNQALiXjIGqA::aywK1f1XZzCgGCoHHwIr:+9JDO5M+QvdH/0XN::azaV2s5sIK8s7nO0Zzuq:OOdrW0uDwQgthZ5h::14VcYYsh+LAGvMrr:PB97EzuUC8p803RZHElE::p6DSFlklETb3TeapKQk0:UUeAiJLct0GUC6rS::RSHPMvAqntBwZdM2:WFZLM2y5q1iYHZGvdtu5::76zpVp6lxX0FMotf:old52qMZ0OhYnYEKWLwF::mE9DMp5xHMqMihqsNMkK:pTe5a0DPczD1eP6q::ut9YIGUhLfP6uIiIIGAK:q+T7HP6ktSDKTyQK::rhZBk6qJmHYwhtTG:YvgGzzjttA34Bwqhl8ex::sDCls0ka8RTGyfGlJWSn:W5WAW4/UPu3b5ha8::U0ahFMa1Z6xYmYUV9RGj:XXySYJP+KPOsxFrK::CKriHdbqx6ssFoYD:Nmwq3O2kvU9hTRJGx0yZ::qlZMYN7HOa9O4bzmDHB7:0HlpzRykrkKPLLW4::7FCV9fkNxdgYIj0Gwnsd:rpECFGeUmUJcGY9C::7jtl00wpwFqMM9LOeBWw:JaXZSDuZsfK2ACv0::OeduytXY7JFqk+6+:YuYv2aoBnsZznlaBxNCh::MJeRhtCtOqYy7LN0:5sXQzTy6Pi99QnUKToxS::B8dw4l/YajMVeQAy:QCoK7AIf5U0kEUgaRCQV::VDvXxFTTR6dvRFzISDl5:uCPlpLhEQDC46mWt::4ll5BiPjchsYUuPu:os5taGeoyd3FihCFXqKN::qSJSkYljDnwhuQ0z:81sQFLai71rE8jqmOoT3::nWxetxOLhbUQjiBb:bPZE77RRoealSZTYuboU::gV64wteSdzSwOQIylDYj:bkrmp6IMKE4cGZBs::59+HyyjWa3a8aQST:rSX5wwafzUKDqetIjNm4::f//nqv0FdLfSKJI7:CBodeKiwrx1ZC9pLLbKZ::8MbzMNBVNo2HzUKw:pns9TQLj2f5DR6mg2jTU::yVjhMY2I7FUJkdJf:aHbVPwhlU3a1awj1zyTf::aYn5gtVGquNFhP2ZclPF:4myeo/pjaqAyP5BB::NMPycv7677JZWd7b:S4qQmtYvK0uQMiLD3Fch::lm90SwVR3NxqGm0D9Rl6:CtGLdE7GDz06EHN1::FCIFzg/ptmw7m0u5:5QcVPYmJxCwZqxZzNJtT::aeWPHuR7nFKnAdHn:8H1eAflyOjCmCUpFXtZs::oNO/zUbEEKbwW03E:Uus8WSKbhUmGZD8FrAxg::TEq4owooO4Tf5bGkwt0w:RzTxPmeOJYTQBJFu::GK0keyz6ei8u0ZFs0qzW:0vMApVukb5ls1GJw::u8lUD6kysVouZ6F0xhZf:viR/Q5DFY4BfBzfM::A0QoEedkvrzAG5LWXlMW:+UqatznchsH9GoIQ::wVR1M9BAun3UwGjk:UdOL3ITNHtsGT8hoIJ8m::pb6LlewV5qSMQYlcoBtx:ZBH2ZaBv7oIognfu::kjPH4BKjEqZ5KfYKk8AX:YR08pMf6NLQFBtvS::kMWsJYvWOnBGOlOnzCLc:GxwkeyeD/QWZ8SXM::xXNZFeOMoovmkN1s4xpM:UuZmudVTN0U/O83q::NvVDHQ6zmIRZG9FFdOQ3:6dniQlUyYF2AbnXe::cKg6tJ6J4VcTORKcJY6A:PsfjB9BB48AZiPNt::5j8vbbWVQGESOgH7EZy5:6J6KXzpNv36tARB+::CkYQcv65INXdDD1LKiTP:TLD1NXfyJnfVuAkH::3esF9UcSD9lSK1Fu:gHAsDZ66WTyNukG3ReUT::9AWfjRiVZYDPSonk:8fYuXJrKWFXTDmYzzZIa::ZU1XwbXp03olp3ChhF83:MXwzrsM5+0fIGfwf::slszAKRnlLuULflK:vooB0PAYbr6JxPY7cpgq::j+a7Jo7+PA1H5FKb:PalbeJYxkVLbunD6v3ld::d2TEqeMSuHht1zUkzpxb:wCzANIfcMJphNTCk::ZnNv7SqeQArH9WExklXM:ZHmAqBbqrkZJBK2r::eKMekG3TQPVG341x:DXjUguSRKADYooJ8WUE4::rHWiLUsZ0WpJDLRd:iG81BqGnvqzhMmfx1HIJ::BdHUxKAoApuEvnowQyxO:j1CqJ16JcMggsTzK::t7OFQZNNsbxLIQslhtlU:Sr7aOXvvKf9Zhjq8::LuUaDsl11f0inaLnOmqe:9JlgSr5ShEDJVYdh::kSeowGDjMTnCYP0j:0fvsvwUpfZGMdcWXvWyD::Vg9BqYomr7Vs0aIKN48G:G0QG2PNdz7qaPjrJ::v7vDFlZQC1pqVnkC:3OsK0mZzVs1BEIvBTEck::YpQzLjxN4hPf4CtfypHv:JSx5CDM/+OMc68e7::Kc0OqVN+If/GKySF:Kgv37jJSO3tGhJcoYCLE::KTpExbAeKbHbCsx913fm:dXiuFXeFe3pMzQ3G::g3NSNGBrqiOXv5OmPCvK:yMR4jSnLCrRxRrcN::H9MpuHPkEX98XWll0vtP:X3WFTkCy3NDHLrN+::gRELMnpOeUeAGxS6OrkK:yn60WClbyLgG4YGP::OpTJejVuryZRnTpq:ZU0917P3edmX0GfKrhzf::rbtXydJy22qOiE5S:eMKfbSbosnNIM0mHsSbJ::/wueySgKtscKK6vL:WD6HeJsW5gtvzjBKD32R::qwY4IOqGo5PR4s9bYGJy:yfemQREfoPsSWfzd::Ytyuoss9idnqBTaVRGz1:e1amHNMCq036cBXJ::ugezYlyRmHIQax4U:DOUDIwYpXEcRDmA6Uj0E::q0EtNvqQwZ4PCsoU:VKVtB2BoIUcQmzv5qwu9::XckNy44e9i9kPeZD:kjKYK3RjczJNydE878Ui::RpPZJMarljvIm/e7:DOl10oTKNgYxmmUaoRZ0::94CKMQmhLM0V5DTVkmIQ:Wiu52/JpFhhxnwvC::2lrJepvOIYDqpU18aoAf:VDbhtDV85nOefVIStGdAlkNzXxTbmcxl+ipPjC7XdLYk8L4vL22v/SRXO9hBRxt9FhA6smZVVioZlIZC8RJI3g==,oFKtOMSsL5NoeE5mFUMSVFsbeRJxSgcES/3DlH/V2BQ=:3Jn/GX2ycficclPnC7bLZym8dtKFxDAVegB/JKPEs38=:y+7LxcqC6sSN1Ajitkpizsavlm4Tr34Rnge0PJ3MW9U=:fktixHpB0Uf2KBjDf+OCysIGMfrKxIECBIFkPSaEtW8=:J1lcBhEH5Qx8xfmhIRtWHWM3BVFk7cDbqLMZnwh3J2M=:ptlUZB78Q0/N/b2rIoI8y8WRiKwBG4/NioNVZUjeius=:BXMAQVXiPOmN6s7HAJ6TDxM1NjHpidW84uFEmw1vx2o=:z5+V6HQ2W7qS7eDU+dHFpAoprbXudhGbWR/k8zeZZso=:qbhRJcGcWhxgZw+cwKbDPVSFpfNSk5Hy3h2ebj+CHPA=:bt3Nbc28sJmQErHL1SaYr2evyhUdxfrSoAB6pYmZ8iI=:37vFuBWBpBEVrA1YG2AH8ggUZMVFkTx3pXECSEhjOhk=:ANVu9pSAA+SYgN9A+/iGX7Nxw262owxjazJrwAgKHRg=:3GH/1qhnyGpMtcAJHxC2NS9SRuuJxQVPO7ebEJ+WJYw=:pMFfda5uFbXgd/IeScYVpGd0tV7vDyInrRZUP/SUldk=:6mQ+wTLu6w8thiV2Ly0FFDi1/V2RpbMbRp7UTbrqFnA=:pKHIiNKGehjvI2njqAyVxQYzOKmoSbQxzZB1AVfiYKU=:2ONHUHMWvTjuML1x2uphYYvCxpdLVbASpj/E4DwSQOg=},{0}");
                //Block 1
                out.println("{1433745705904:1:4F5BFD86FD26F234F31694954DD2372F2045F47E02B346EB7C50C259E0D86A64:150000:9312917},{0000000000000000000000000000000000000000000000000000000000000000},{},{C5KKYT7AURETVHAKJ2G2RXNVMA4BKH4KQWCJOW:BLOCK_ONE:90909090:CureLabs:1:4F5BFD86FD26F234F31694954DD2372F2045F47E02B346EB7C50C259E0D86A64},{Yt3+jcZVinzqIVjZ:8PDZbRXYyhpH6Bhokj5m::N+uVg3NL8SL5TE4C:5nZywxRyiEo4jYSvKTP7::OtdHNePkarWc1bM2PwGO:21zQd/+Imm09qaR1::xXrc2OLWfXCdNroOlSsw:bXReU6PX9M0Cs0o4::gjFvqFkrcyb+wxpP:XUQPwM7z3MS1YcqE4Fut::c5iH7NU819nSSaIPHAyX:+M70LA4Gs5UYe+fj::lkeJYvApr+p4OOPw:CWeGWDECEgRIAHJLf53O::kZw0EMMOMx0ffPBr:WFBZONGLGGU96F8RxVw5::HwYlCKtpvg3wPgnpba8X:JeSadCw4H9ok1jP/::P8GRmPrVgfley8AEI3Hg:M4L8mm3zArtkTI5y::FHmpG5bt/Xon5ocx:s6bGphpWey4sWVLoN4bD::EL1u7GfyDqvx3EZ+:NDuKwVn28xT5DBpX8OCQ::9ts1OQ1SvwrtkgQA:8SfU16jR1gWepPs1w6O8::9zVLfvOATnpTwxsZMOma:elJK6TtguLzr8MJM::SSN8P0xhUF5lDIhYiAi8:M8J/zgOIBoGUJB3g::GIj9oBT6lW8XDVlKtT5M:8j0p1+EehTRl1FyG::gxJ7ccl1Z4K94RV7:5HjUFM9udR04Y0LfIPSb::O2Jz2Oai5N6rJXqG:2bsKVo0QzWUNAcKlk6zW::HdaTq76EVodbuOb2yAGs:10EkxrVZRep0kcD/::p7277rUBy9yX3LkBlMov:6SW72Y9aIgcNLBAR::1tEmwxLaOSdRRzpL:ZfbOFIVXT6i78B4OKrfY::XZzl0QG5RtOfKp48qtuV:Q7oFGMbbvRG6/4Iq::jhcQ4GUbtLtX7papfXWC:E3N7lXXEoVNcy5V6::OLVBPp8zV2KbJdGvJ2BX:yJguIa5vmPWrdru6::wzHEdvlNs2ucI3nEfjEm:eKKIn6mRQse+V5x+::F4gmsPRYKJJzZCqTDcMZ:vY5oGYkyQpdoEL4R::6GokjL0BgDvBjRX/:rxQBtUq9AFbJSFz3TaQa::fD6eFxQQ1Qi2wUcd:wRMih8gDBHUtpVKXLQjB::w7WhTwcaqxziacBO:GjLX1yQsyIL4WpfNdCHm::5fEf+0Evy9AnriI5:FNOEDqZ6CSvBV8eA7cLI::p8zEVds3JrkfOqgogDkO:p+gXrBAEdSjL7RHK::wvhrCFIzU5BUApRggVnd:71BhjpXBZuB9UcKL::5zqqa6P6lPwLcIyCuCla:HC8Vb5MDVcNCu5ty::ZaoEbIR0XIsbJ84kD7AO:HLc//WGPLcNGSHIb::GD1S50YIpylZXqIGQoCs:v0SaCGCq01a3bHWz::HsC6yBSciR3S9bBM:KkVixOkv1zA3KJMKM8Lj::G7Xf6aWWk6651OhN:FFhNx5GoP16Hz30plSay::TFfMa+/90AyaMXIM:kOX3HA8THzuXHDgI56qr::W17QpLUlc7mG5I3j0rC2:CebJVZx4AJPtYidm::+STpoT2GY7lTkfWC:04B4DRfgJjvu7lBspMrp::KVDXcf1gN5u6YRzc:r9OHqhn3n5G87sgkhyMC::wxHAw1/yCkmFYPs6:s9CVClpnvzKBEdqlUZH7::4ssu4TS3WnUVKrGeupsK:zA8kFBeJDd8x9Wdc::1o2XMcNWr4uevfKC:GAiZSM6rRV01SmawyDpd::iMhHpND0jsS9Z1XxKc8Q:pCcRHvpIXYOtz06Z::WH3D7pnw+DczLQGe:zIT7wSBMdikJyXNxXWbH::yGjrunVNpIzoeXILeKzF:4CQwop9vQYVjip25::vUM5ALYIGXHHgL9pTOZJ:j/i3Yom0ka2/HC8L::KGCmvSVa9Zbw7Xx1:ZtvdR27QckySsAIJRNSK::yWIL0irFIScYsvEd:zkdWpG2vOCczNxTseSBK::ArCgx0ekh9RTn8Qc:Ujc8M9Qawoov6UKz9j65::QWPixXQIerpvBO3TggPr:v3rx8wHJRM7B4Jtt::zOSZ3AF1AZWbZvSk:uFyY7ZezlF5DW2nk1m5a::NvWGjH0X9oUFkoNl:zEi9pI5LWya6dyTgqshK::PbsDwlVSXng0iK48xNRV:5G9pK0tTCnBgKqKQ::lCoVxxqITBGYclF8iiJE:gEiRYT8gnQHE4u2g::LpYY72psoNqW5N2w:2p3J8lDszQhzf5OhWeZ6::QnSrSck9m5eiyFZ9rhLe:6i7/oarEWQxUVBqe::dnaYBCdagELlvAZ7xgEC:2x0mzGZgF59rW8r5::TEhGShujQmg2Y85X:xJBaS8z0rQVZNcRIfrQM::5zGrwMX2Q6/cOUwm:R8322qmAnMOGLEitozmG::9fQ8yHTzIOOWXlzn:lkHbDJwWZ4coAPpYxGkd::Owhn5H1lJIX34ble002B:W2wvbxFWlW8W0bXS::QpELCngz+rbzpdyI:GBOCivwgxLTGsZDCa0F5::LrVxTHwNlI/fGcHw:Z92d9UyUTS5rxY8Q0JZO::3r8OfiBncE1MlYadyKD2:TKFIOE2DhYobhtD/::aVTNURFoRb13MDFZ7SbI:058VF4HUTsc3UvyN::fm7gFODHQAz4LXUm:J6jLwhFUM1FgJbvLIELi::bqXF6O5xbTG8Fn8xgfx6:0Z1wcb2+HWZbQ3qb::Dln2SXSJj5MFqPR77dzK:U8xMKtkvhK4sXfpI::5htD7c0WJHCp2fJmGav3:eXwpR7k9HGzTVXpD::Ry6CUM56Nkt3PGC6bIyG:JBNoh99IA/pwdk+/::AXR7ZxRTLXQ4drGy:Gr0kJPW65rNVXS0C90xk::+aOylUFOBqbnHXTC:l8u2dLbArpoykPpnhk1D::rftKvq3IjHEvSyTo2xyc:7tuilt+daJvGSs0J::6UOw97r5U9rWQPYd4IYA:JBy7M+A/+CPX8Bix::orZ476eDEqsSN2ZcMWHe:lBRV4k63SFR9uEVV::/1hFr//Au2w8hDDr:RcYJbrBiQQESz8lukbCD::uY7xlzrLYj14DNBG0n4J:yipgAQJRDZwsOF3q::vpbCIr7pArhQhCk9asiX:QgyBtGsen8YumUzJ::vvv4MNVNUUzsDN2keGRk:KZ2G8cPwIvcACxFi::eSk4AmpAIwZazdDG:uCXgKoBKEKV0MVjc6KZ9::DJUcwVkFuRIc81/C:fQ3OFUatmvEvtzFCqiNP::qvMUd9Gvp3HBjyvVRwX3:ye/lET5mfhUTE34Y::O0E05kGgkYvOH77E:L9UKFQRzqjKAXFrmdqMa::omRo1EM2OWWHHzbK:igSfwWwn9YW2QRv9ahI0::oqGhmjETJiK1c2PdtJ4t:fimWZ2rdEWBsTUx/::66+Y0dFHc0FyRwtW:GWQSlJh3hGX0TnYLwzwJ::6i2ng1Gn4pFy5T847rqi:bQef0RW97PVw9DcU::LeWidaiauK3oxR4tWOzj:d+pFxXDAtlO4DjNa::wteXHraU4OwERyUEXkWY:bItwJrAS6eksDUeU::1GQdu2R1Ssky3+tx:gC77EGmPMoghsstVSJiS::xCgmUg30PaFEz90A:Du04RPkM64qV1LzzLwIP::aDtdJExW35AlkZndx2jY:StrdTkjmecU3ruSp::nACQXNgKxCiXkHGp:u5R79Fin1tuCZg1pOWL7::8srSselReQGr/Yb5:7tu7N8tJCdMW0XEaIhKb::X4a6YQoeqbnkQnyIWAa5:EmFS8xFxmbSBTLKg::BYna2ORwYFBqCtTS:aprQsyj2n5pxhf6qUk2o::5hnx2S1sPdFrYfjIWcPf:f4ztipcQVnNhmY8K::qWwaVFwR8x7pyRwTmwXDkel6VSAXbPRkptimgM33n/DOH/9dscPo0osEbHj0h9WUa+lFK1ctOxjwPU12w9BI8g==:CFFfnABB3xuNb0yZqV2D,UOJxiqX8eaPWPZNd87el5spZ3C3Nzfxy2WwzhPManQ4=:EAioAcR7eP/rQzqzp9WhRqZ0thWoq2Nd2MFezgZrgC8=:vU+/EILhNkIrY8e/lhccPR/IisHIFni9+efQSMuVvvQ=:C7NYuk1zAechmHQd3d3+sOhJsJBVWlFoaIAYDuhFCVA=:JC4euxAtKRUnKQwCGz2VIepm8BkpcNwyCbnKFT972Wc=:5z/r+i998uJGObP7Q2eZNH6s27yLdnrQ2Jw6B8E5qCk=:ZcrKFOngJZXxO6I8zWnBpqRvXap7lwlavh3TJUKp/cg=:DM4dvareR4N0+XFdIKJzYjV4f17KM/1rCP/48rlu9sE=:04zBfdP+5lIFilstGKqLpQXP+s5cnNXIwXX+unTe6qs=:nQTxUC7G+Dm8Xu5cdrO/WAJKNoBscKtlNof24emrZVk=:Jh3RHp8+r8fBsUuFlbDZAONatBNgVvqndrXf6uPrIgc=:1DO9Se2lLQtvgrAhOOMAgWUwxbOFMIIcXZKBeQtzoqs=:ykOgoDZGiFtNyMein2njVM19obIohjOdCM/HAwuQa7U=:VjvhVqrVlcSeOd0QR0kc59r86m1LNIEWWAh9cfTMDA8=:OgWfcKIq5OuGKi++W6QsRhPbbUel8tgz/DJZqnfhKx4=:1ckSqYomP2MeTktHD0CM0U0H5esFOEJTjMLflArDAtU=:QXRlbpthSYwGyU/SUnLFpdOQ+ciax0uDVQWe1lGnTOc=:kgPYvTDml7pAq/7XCNqv9kfydccRH4ObdMU0uAupJek=:WTyuno8Sgfhu/TWy4VfyO40hK+0k12rmrpnUJJ2hU6Y=},{1},{C2C700D49735291DC3E02FF3BEA7E60BEDE4671C59A30E6B44B55CCDCE085661},{2l6QSfeWjer/E9xe:ZRKCICTBWXYM5R0CTskl::KV4k28IfeK0PBTyb:bLBSxNmWQ4J5XHZWbDIV::c9pKGp74CjYXHgnD:5jqGG2l7JALZyCanhdpv::8fyUTY2hSGreUG89p4nG:ZYy3D/sRTYzHoPgy::eyB66DRl5d3ByDVSsl04:ImcSAJxybCtUUFv5::1G05tO2aZ4s0OpDy6xN0:1EHevJp3tZ1i231z::nCKIHOSkFM2qDaFq:m6tYYRbfFow7FNszG1Eh::trZKRgBboqokj3pW:bidU7EUSSnng1Opg77tG::jmuhsiiZqMwce5Rv0DrZ:mnqtF0d7p5jQKix6::ePfKOJkHKhqNWnZtQMxi:nXAjW/RdVPUXYZUo::csBC64BQr9vlYy7I:tevPbaoRuacilztlM7Ql::J6MUCtxGyrGJRdQp:IAE7TC5gtFYGW6FfHXt8::yvu9KeSeuGMpudXq:A6zBrhUzmQ4q1UT07r4V::0oHTu8YwoSYqxYzL:okIpF42gNNXvY7IbQlav::cqJl68vfmz6SCzvRoWLN:Wr+XnjrWLS2v3vDt::VrAB7s9MNfEZJpOu:vLYqiEoRfdMPE4dNPdQr::7M/4eTRC5gig59tS:8AeKPPuEU3VW7HMZZn3f::5icfk+wwAdG0369U:uRnaJ4lArlvNHslaeVND::wVmLgZqZEU5csaqWuTdH:JnlmAuW9PTYNAcNp::EqvmKPsgjdlirT67MZYt:/FySqfQnszqBE4Bc::tah9Uu2CwMLGdAKq:6cvuAmcqHPjkFVyXKOFW::FCRiagdXQ2jC9xjLq9uw:W56i60Hmm57Yg+6p::wv7yNthdRODAhIRsQ6x1:YqVPFY2Aw4cP+kVE::cGCfutL4WjQfWpUJ:j7nXxPpkt2x8mLJqMJjv::oIo4DUDFVHzyrOpz:jd7Wyjz5oWVIEWHu5w7d::cgwyaOND4o4JfYTyH53e:xW1NRw6s40khx45c::lV2GLFa0HKOI0UxnHQsW:uOSKLbn6t0CaElq8::FIu0v64du58uJmNkVpEz:rvMtkxG5G2YYY9em::EJNqEcnLJcXZ71aI:m8l2QKrEfVjCFb2wNcKU::F7sxHOOnrSvkPFsK:dTM33qudMdLavTlznfiW::ZQJLvcOESVFD6QBQ:qo00TAi3HnxleGZuooO6::WO9Db65MUkJctfuhHM5v:HdrvcpGRKXVbmsDr::vJ+W4J2sBaXIpYFq:g3GOVK10PxLgdYOj5tEP::Qme1x8KV9tOW0vUfg44O:oDI3JbszoJOHt3Fo::ItblO8QDgYi8PpGP:R30Xl9zUeVRHqnGe6018::To7Az7zLTX4Pxnfi:oVCEo8oHQXv0rYIahRAr::Q3XOeEFjv1LKYip00uBu:kSlX/DXkWx6Nv3jU::1uI7WNH+K9YxFG03:SGQG7rqlFzZKntZNvRrX::WQiErkX1QBuX1Y1qEouI:e9fvJqAV6cX5UzeU::C4mhMQvQQCjBZ2EN7iGd:SBGuakdgXcRafs/Q::qTzPiRPBki2T8lSk:JewQfVnGhYs7dcHGPBA2::pzOr8iF2WNJHOXz1Q4FB:obvwUNrAB+zCCoV8::ra0Ed0wFj6sT7SXWF3dt:58sR44Ln9jnIsEwG::h6HEQV7tQqQNn67giz8w:zIwiQZSnyTPSXdFw::Jhj7pDmErdGBa28M1Je7:VdHyV6ebsjTuMrP8::KpXlzXYNo6ks0kKAYL4X:zjrJAZKCt+G6VQ21::Kr8wLdbN5/QSiYiO:KmuoA2cFdS0IeJE6ocLP::FNrsWwqk6w8QHykRk1UF:TnrghYsh5fQLZTVG::dO19biSK7T4uE6t04LUE:pavvmv7ylG7KM0cN::ESArMNcqm4WqodL5:jDMMS1qTcBjKEESmTdni::6f1Dzs5mH344Yz0i:v4LWjZIvV5Z2ydzArTEl::b0jh9EDWahU0dLPXaynd:H+PB6uVsPTDMra9T::mNTYptdIcqhe0ztqa92E:L52cmuwg/SFDp6f3::ZxgCVvTX3YNVQReT:MBaz4betd5ftTzbKdI13::Fxioymd0Gc9WTLVQSTvs:mLYWW/MLwLl8H5vu::trSHKFUiyT8DwKCVn6MR:6CIbrZXA4Jle+M8P::Y4i19FfdJEzda5Bk9eZr:wQ+FaNGpuZLjqRhw::w7sqp3fQAobyvFIK:2uOCO6x0BkDqirVhyMHa::WQYKOuVKC3cZzmjc:J8ZDCCoeSnl5JAnXZtG4::JFo5H4TQyG8RsNJg:fmXeFACyG0WV2DzC47LF::epv6NyqUVpwYUChg:U3CtFmDJRdtyaBgIKncq::UPrRULnwH1ftz25o4Ici:lwx1IhgyRD8+8Rqs::KIAD8+T9e7xrMw8a:987EoVwI485PzE3b0JNH::ag7DS5YnQh+0crQJ:PywsX07kEuX25mN7DAnW::omr12LVlTM4/EU4z:M5OQMwtbBpXeHpRF2kzw::EuDJhUPlIDpFBwOzIkjg:vPSqr0P1dofP+HbS::MOFDwjp+EcRnB1th:BzNvIgJNSCpaQrup50Ez::064pM2pW0IYVPifh:m9pWGWKOPVJojMqOFVnP::Ll7pfT7EuQc1D4hb:PcYW2tL1TgYT5GuG3tdh::w7Rpjb0vsUxljC1b:juX3VA9ELUWVnCE0Bw2M::4FOczhPJVoKmixTs:vEgq7Db3yvtPd728sqXX::GjWrbv7VGPmyvFtTeB0L:V20ruxrSneWdWaFT::pd93bJ2dR36WtdxSoE7R:g63oYPjKPARB7Ce4::rCqp3xdJREaqWVqwlr4p:WPgfEVaomgAVLvnT::FMdqVn0Xh38hae4ztOSy:J6hdFSdkO1bOdJZB::Lo/ah0A6meQo0eIq:JDkJB8pISwckxHI9sTvz::UlgeUXIbt0op7epz5J8R:7qu75+R0tZMdD3wN::WiXduZgjgQSQAfv5SvRa:kQridZmynVW1uLb0::tXxEjbX72lKy7DtdJhUF:hXV/vJy21rHUGct2::ZzJyTCaHtVmnWzU8:ZpM5O2alFrrpeXxLVvdB::R2j0SW8XUxwoUQ993EpX:t5/laHNU6/umwl+4::aTEEODXZa543rV6q:5gPA0uvlzyfKVYu7mShP::XEkmyRQgUtsEjhdyZZOH:m8WA22QbZE7TqNcg::3u2MUn78x5O1K6TL6SwI:N2Aj5/t/qJZ+n6St::5Tz8KNtUU1gZmUE6:oDBnayPakwfwkWO8Ir5w::9qCAvTU4GUjIPDUE:AFHovx6kYSlqpYCPW0e4::SOkFmsvFB8c0uy/l:kyBTRzPIbuIGNqWPvCJd::Cy78FTo1Re+BPu/R:3DBFwe4EOGeASvZolvt0::rl4BbbzVUILEtI4EYyoY:rBBy2qIRhwXmbEyY::whg6j3TPEGrEQyCJ:75mVVlHe6Y4PRnzYv6cz::UnP0PmldHgIgzXwh:ZrobkXB8AdYYIPbQwlfN::nyTqbYnSfsawNkoIy0Un:knT/qJLV4wA6xZiO::xol0RWdDMtEKMBMr:jIluMbHPmipNlg6MgQs9::u9nNGzoXnVSQRvMJRB6b:YeQrx6qh3idgI0Dn::qwhFZ2hXD5XM/0EC:LFFvZeNF86CrAfFpZ69h::SlWHD6zAvbhSzJngFie6:hHaVkONS0ig1u1Xt::FgaeY6nBhNJwvTua:z6ZMfTjZd1tA3BxOTwG1::b4fdP++X35Nt/+W0:3lB55gcsCN78wtIBamRO::pxeYyNNlpqEkfuV10SSK:9oX/4lAPYzJRaT/z::ktUmlaJ4OwKYfAdhTY6p:pThtk9sWmcllKtfEjva+6OOsp7QzlWyz+XvijlrY2c16EouqazeiohT505glg8uENVvX5hn9KBdc2lPTM8lXOQ==,6tvmSRlfr/oS7HTLw/SrGBF1XFsm+5meLuFexCoTogQ=:3Jn/GX2ycficclPnC7bLZym8dtKFxDAVegB/JKPEs38=:y+7LxcqC6sSN1Ajitkpizsavlm4Tr34Rnge0PJ3MW9U=:fktixHpB0Uf2KBjDf+OCysIGMfrKxIECBIFkPSaEtW8=:J1lcBhEH5Qx8xfmhIRtWHWM3BVFk7cDbqLMZnwh3J2M=:ptlUZB78Q0/N/b2rIoI8y8WRiKwBG4/NioNVZUjeius=:BXMAQVXiPOmN6s7HAJ6TDxM1NjHpidW84uFEmw1vx2o=:z5+V6HQ2W7qS7eDU+dHFpAoprbXudhGbWR/k8zeZZso=:qbhRJcGcWhxgZw+cwKbDPVSFpfNSk5Hy3h2ebj+CHPA=:bt3Nbc28sJmQErHL1SaYr2evyhUdxfrSoAB6pYmZ8iI=:37vFuBWBpBEVrA1YG2AH8ggUZMVFkTx3pXECSEhjOhk=:ANVu9pSAA+SYgN9A+/iGX7Nxw262owxjazJrwAgKHRg=:3GH/1qhnyGpMtcAJHxC2NS9SRuuJxQVPO7ebEJ+WJYw=:pMFfda5uFbXgd/IeScYVpGd0tV7vDyInrRZUP/SUldk=:6mQ+wTLu6w8thiV2Ly0FFDi1/V2RpbMbRp7UTbrqFnA=:pKHIiNKGehjvI2njqAyVxQYzOKmoSbQxzZB1AVfiYKU=:2ONHUHMWvTjuML1x2uphYYvCxpdLVbASpj/E4DwSQOg=},{1}");
                out.close();
            }
            Scanner scan = new Scanner(blockchainFile);
            while (scan.hasNextLine())
            {
                String blockString = scan.nextLine();
                Block toAdd = new Block(blockString);
                if (toAdd.validateBlock())
                {
                    blockchain.addBlock(toAdd, true);
                }
                else
                {
                    System.out.println("[CRITICAL ERROR] BLOCK " + toAdd + " NOT VALID BUT IN BLOCKCHAIN DB FILE!");
                }
            }
            scan.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Checks if database has bare essentials
     * 
     * @return boolean Whether database is in a workable environment
     */
    public boolean databaseActive()
    {
        if (!dbFolder.exists())
        {
            return false;
        }
        return true;
    }

    /**
     * Checks whether an address is formatted correctly
     * 
     * @param toTest Address to check
     * 
     * @return boolean Whether the address is correctly formatted
     */
    public boolean isValidNormalAddress(String toTest)
    {
        return merkleAddressUtility.isAddressFormattedCorrectly(toTest);
    }

    /**
     * Returns the balance of an address on the Curecoin network. Does not include unconfirmed balances.
     * 
     * @param address Address to check
     * 
     * @return long Balance of address
     */
    public long getAddressBalance(String address)
    {
        return blockchain.getAddressBalance(address);
    }

    /**
     * Returns the signature index of an address on the Curecoin network. Does not account for previous signatures; only blockchain info.
     * 
     * @param address Address to lookup
     * 
     * @return int Signature index of address
     */
    public int getAddressSignatureIndex(String address)
    {
        return blockchain.ledgerManager.getAddressSignatureCount(address);
    }

    /**
     * Increments signature index by one for the provided address; useful when creating transactions.
     * 
     * @param address Address to increment signature index for
     */
    public void incrementAddressSignatureIndex(String address)
    {
        blockchain.ledgerManager.adjustAddressSignatureCount(address, 1);
    }
}

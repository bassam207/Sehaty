import React, { useEffect, useState } from 'react';
import api from '../api/axios';
import { FileText, Upload, Trash2, Share2, LogOut, X, Check } from 'lucide-react';

const Dashboard = () => {
    const [files, setFiles] = useState([]);
    const [selectedFiles, setSelectedFiles] = useState([]);
    const [showUploadModal, setShowUploadModal] = useState(false);
    const [showQrModal, setShowQrModal] = useState(false);
    const [qrData, setQrData] = useState(null);
    const user = JSON.parse(localStorage.getItem('user'));

    // Upload State
    const [uploadFile, setUploadFile] = useState(null);
    const [displayName, setDisplayName] = useState('');
    const [category, setCategory] = useState('RADIOLOGY');
    const [subCategory, setSubCategory] = useState('X-RAY');

    useEffect(() => {
        fetchFiles();
    }, []);

    const fetchFiles = async () => {
        try {
            const res = await api.get('/medical-files/AllFiles');
            setFiles(res.data.data);
        } catch (err) {
            console.error("Failed to fetch files", err);
        }
    };

    const handleUpload = async (e) => {
        e.preventDefault();
        const formData = new FormData();
        formData.append('file', uploadFile);
        formData.append('displayName', displayName);
        formData.append('category', category);
        formData.append('subCategory', subCategory);

        try {
            await api.post('/medical-files/upload', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            setShowUploadModal(false);
            fetchFiles();
        } catch (err) {
            alert('Upload failed');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Are you sure?")) return;
        try {
            await api.delete(`/medical-files/deleteFile/${id}`);
            fetchFiles();
        } catch (err) {
            alert('Delete failed');
        }
    };

    const handleShare = async () => {
        if (selectedFiles.length === 0) return;
        try {
            const res = await api.post('/share/create', selectedFiles);
            setQrData(res.data.data);
            setShowQrModal(true);
        } catch (err) {
            alert('Share failed');
        }
    };

    const toggleSelection = (id) => {
        setSelectedFiles(prev => 
            prev.includes(id) ? prev.filter(fid => fid !== id) : [...prev, id]
        );
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Navbar */}
            <nav className="bg-white shadow-sm border-b border-gray-200 px-6 py-4 flex justify-between items-center">
                <div className="flex items-center gap-3">
                    <div className="bg-medical-100 p-2 rounded-lg">
                        <FileText className="text-medical-600 w-6 h-6" />
                    </div>
                    <h1 className="text-xl font-bold text-gray-800">My Medical Records</h1>
                </div>
                <div className="flex items-center gap-4">
                    <span className="text-gray-600">Welcome, {user?.name}</span>
                    <button 
                        onClick={() => { localStorage.clear(); window.location.href = '/login'; }}
                        className="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-full transition-colors"
                    >
                        <LogOut className="w-5 h-5" />
                    </button>
                </div>
            </nav>

            {/* Main Content */}
            <main className="max-w-6xl mx-auto p-6 mt-6">
                {/* Actions Bar */}
                <div className="flex justify-between mb-6">
                    <button 
                        onClick={() => setShowUploadModal(true)}
                        className="flex items-center gap-2 bg-medical-600 text-white px-4 py-2 rounded-lg hover:bg-medical-700 shadow-sm transition-all"
                    >
                        <Upload className="w-4 h-4" /> Upload New File
                    </button>

                    {selectedFiles.length > 0 && (
                        <button 
                            onClick={handleShare}
                            className="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 shadow-sm transition-all animate-fade-in"
                        >
                            <Share2 className="w-4 h-4" /> Share {selectedFiles.length} Files
                        </button>
                    )}
                </div>

                {/* Files Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {files.map(file => (
                        <div 
                            key={file.id} 
                            className={`bg-white p-5 rounded-xl border transition-all cursor-pointer hover:shadow-md ${
                                selectedFiles.includes(file.id) 
                                ? 'border-medical-500 ring-1 ring-medical-500 bg-medical-50' 
                                : 'border-gray-200'
                            }`}
                            onClick={() => toggleSelection(file.id)}
                        >
                            <div className="flex justify-between items-start">
                                <div className="flex items-center gap-3">
                                    <div className="p-2 bg-blue-50 rounded-lg">
                                        <FileText className="w-6 h-6 text-blue-600" />
                                    </div>
                                    <div>
                                        <h3 className="font-semibold text-gray-900">{file.displayName}</h3>
                                        <p className="text-xs text-gray-500">{file.category} • {file.subCategory}</p>
                                    </div>
                                </div>
                                {selectedFiles.includes(file.id) && (
                                    <div className="bg-medical-500 text-white rounded-full p-1">
                                        <Check className="w-3 h-3" />
                                    </div>
                                )}
                            </div>
                            
                            <div className="mt-4 flex justify-between items-center pt-4 border-t border-gray-100">
                                <a 
                                    href={file.url} 
                                    target="_blank" 
                                    rel="noreferrer"
                                    className="text-sm text-medical-600 hover:underline font-medium"
                                    onClick={(e) => e.stopPropagation()}
                                >
                                    View Document
                                </a>
                                <button 
                                    onClick={(e) => { e.stopPropagation(); handleDelete(file.id); }}
                                    className="text-gray-400 hover:text-red-500 transition-colors"
                                >
                                    <Trash2 className="w-4 h-4" />
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            </main>

            {/* Upload Modal */}
            {showUploadModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 backdrop-blur-sm">
                    <div className="bg-white rounded-2xl p-6 w-full max-w-md shadow-2xl">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-xl font-bold text-gray-900">Upload Document</h2>
                            <button onClick={() => setShowUploadModal(false)} className="text-gray-400 hover:text-gray-600">
                                <X className="w-6 h-6" />
                            </button>
                        </div>
                        <form onSubmit={handleUpload} className="space-y-4">
                            <input 
                                type="text" 
                                placeholder="Display Name (e.g. Chest X-Ray)" 
                                className="w-full p-2 border rounded-lg"
                                value={displayName}
                                onChange={e => setDisplayName(e.target.value)}
                                required
                            />
                            <div className="grid grid-cols-2 gap-4">
                                <select 
                                    className="p-2 border rounded-lg"
                                    value={category}
                                    onChange={e => setCategory(e.target.value)}
                                >
                                    <option value="RADIOLOGY">Radiology</option>
                                    <option value="LABS">Labs</option>
                                    <option value="REPORTS">Reports</option>
                                </select>
                                <input 
                                    type="text" 
                                    placeholder="Subcategory" 
                                    className="p-2 border rounded-lg"
                                    value={subCategory}
                                    onChange={e => setSubCategory(e.target.value)}
                                    required
                                />
                            </div>
                            <input 
                                type="file" 
                                className="w-full p-2 border rounded-lg"
                                onChange={e => setUploadFile(e.target.files[0])}
                                required
                            />
                            <button type="submit" className="w-full bg-medical-600 text-white py-2 rounded-lg hover:bg-medical-700 font-medium">
                                Upload
                            </button>
                        </form>
                    </div>
                </div>
            )}

            {/* QR Code Modal */}
            {showQrModal && qrData && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 backdrop-blur-sm">
                    <div className="bg-white rounded-2xl p-8 w-full max-w-sm text-center shadow-2xl">
                        <div className="flex justify-end">
                            <button onClick={() => setShowQrModal(false)}><X className="w-5 h-5 text-gray-400" /></button>
                        </div>
                        <div className="bg-medical-50 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                            <Share2 className="w-8 h-8 text-medical-600" />
                        </div>
                        <h2 className="text-2xl font-bold text-gray-900 mb-2">Share with Doctor</h2>
                        <p className="text-gray-500 text-sm mb-6">Scan this code to grant temporary access.</p>
                        
                        <div className="bg-white p-4 rounded-xl border-2 border-dashed border-medical-200 inline-block">
                            <img src={qrData.qrData} alt="QR Code" className="w-48 h-48" />
                        </div>
                        
                        <div className="mt-6 text-xs text-gray-400">
                            Expires in 15 minutes
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Dashboard;
